import React, { useState, useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet'; 
import { useAuth } from '../../context/AuthContext';
import assignmentService from '../../services/assignmentService'; 
import workLogService from '../../services/workLogService';
import profileService from '../../services/profileService';
import gigService from '../../services/gigService';
import './LaborPortal.css'; 

// (Helper functions and Icon fix... unchanged)
function MyLocationMarker({ setMyLatLon }) {
  const map = useMap();
  useEffect(() => {
    map.locate({ setView: true, maxZoom: 16, watch: true });
    function onLocationFound(e) {
      setMyLatLon(e.latlng);
    }
    map.on('locationfound', onLocationFound);
    return () => map.off('locationfound', onLocationFound);
  }, [map, setMyLatLon]);
  return null;
}
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});
// (End of unchanged helpers)


const LaborPortal = () => {
  const { user, setUser } = useAuth();
  const [assignments, setAssignments] = useState([]);
  const [logs, setLogs] = useState([]);
  const [gigs, setGigs] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [myLatLon, setMyLatLon] = useState(null); 
  const [isAvailable, setIsAvailable] = useState(user.status === 'AVAILABLE');
  
  const mapCenter = [19.0760, 72.8777];

  const [showInvoiceModalFor, setShowInvoiceModalFor] = useState(null);
  const [additionalAmount, setAdditionalAmount] = useState('');

  const activeLog = useMemo(() => {
    return logs.find(log => log.status === 'ACTIVE');
  }, [logs]);

  // ✅ Find the one active gig
  const activeGig = useMemo(() => {
    return gigs.find(g => ['REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'PENDING_PAYMENT'].includes(g.status));
  }, [gigs]);

  // ✅ Find completed gigs for history
  const completedGigs = useMemo(() => {
    return gigs.filter(g => g.status === 'COMPLETED');
  }, [gigs]);
  
  // ✅ Separate memo for the *single* incoming request
  const incomingGig = useMemo(() => {
    return gigs.find(g => g.status === 'REQUESTED');
  }, [gigs]);


  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const assignmentsPromise = assignmentService.getMyAssignments();
      const logsPromise = workLogService.getMyLogs();
      const gigsPromise = gigService.getMyGigs();
      
      const [assignmentsResponse, logsResponse, gigsResponse] = await Promise.all([
        assignmentsPromise, 
        logsPromise,
        gigsPromise
      ]);
      
      setAssignments(assignmentsResponse.data);
      setLogs(logsResponse.data);
      setGigs(gigsResponse.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))); // Sort newest first
    } catch (err) {
      setError('Failed to load dashboard data.');
      console.error(err);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchData();
  }, []);

  // (handleAvailabilityToggle... unchanged)
  const handleAvailabilityToggle = async (e) => {
    const newStatus = e.target.checked;
    setIsAvailable(newStatus);
    setError('');
    if (newStatus) {
      if (!myLatLon) {
        setError("Waiting for your location... please try again in a moment.");
        setIsAvailable(false);
        return;
      }
      try {
        await profileService.updateMyLocation(myLatLon.lat, myLatLon.lng);
        const response = await profileService.updateMyStatus('AVAILABLE');
        setUser(response.data);
      } catch (err) {
        setError("Failed to go online.");
        setIsAvailable(false);
      }
    } else {
      try {
        const response = await profileService.updateMyStatus('OFFLINE');
        setUser(response.data);
      } catch (err) {
        setError("Failed to go offline.");
        setIsAvailable(true);
      }
    }
  };

  // (handleCheckIn... unchanged)
  const handleCheckIn = (projectId) => {
    setLoading(true);
    setError('');
    if (activeLog) { 
        setError('You must check out from your active job first.');
        setLoading(false);
        return;
    }
    if (activeGig) {
      setError('You cannot check in to a project while on an active gig.');
      setLoading(false);
      return;
    }
    if (!myLatLon) {
      setError('Could not get your location. Please wait for the map to find you and ensure location permissions are enabled.');
      setLoading(false);
      return;
    }
    (async () => {
      try {
        await workLogService.checkIn({ 
          projectId, 
          latitude: myLatLon.lat, 
          longitude: myLatLon.lng 
        });
        await fetchData();
      } catch (err) {
        setError(err.response?.data?.message || 'Check-in failed. Are you at the worksite?');
        setLoading(false);
      }
    })();
  };

  // (handleCheckOut... unchanged)
  const handleCheckOut = async () => {
    setLoading(true);
    setError('');
    try {
      await workLogService.checkOut();
      await fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Check-out failed.');
      setLoading(false);
    }
  };
  
  const handleAcceptGig = async (gigId) => {
    setLoading(true);
    setError('');
    try {
      const response = await gigService.acceptGig(gigId);
      setUser({ ...user, status: 'OFFLINE' });
      setIsAvailable(false);
      // Update the gig in our list
      setGigs(gigs.map(g => g.id === gigId ? response.data : g));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to accept gig.');
    }
    setLoading(false);
  };

  // ✅ NEW: Handle starting work
  const handleStartWork = async (gigId) => {
    setLoading(true);
    setError('');
    try {
      const response = await gigService.startWork(gigId);
      setGigs(gigs.map(g => g.id === gigId ? response.data : g));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start work.');
    }
    setLoading(false);
  };

  // ✅ UPDATED: Handle completing gig
  const handleCompleteGig = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await gigService.completeAndInvoice(showInvoiceModalFor.id, parseFloat(additionalAmount));
      setShowInvoiceModalFor(null);
      setAdditionalAmount('');
      // Update the gig in our list
      setGigs(gigs.map(g => g.id === showInvoiceModalFor.id ? response.data : g));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create invoice.');
    }
    setLoading(false);
  };

  // ✅ NEW: Handle getting paid in cash
  const handlePaidByCash = async (gigId) => {
    setLoading(true);
    setError('');
    try {
      const response = await gigService.markAsPaid(gigId, 'CASH');
      // Update the gig in our list
      setGigs(gigs.map(g => g.id === gigId ? response.data : g));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to mark as paid.');
    }
    setLoading(false);
  };

  const renderInvoiceModal = () => {
    if (!showInvoiceModalFor) return null;

    return (
      <div className="invoice-modal-backdrop">
        <div className="invoice-modal-content portal-card">
          <h3>Complete Gig & Add Charges</h3>
          <p>For: <strong>{showInvoiceModalFor.consumerName} ({showInvoiceModalFor.skill})</strong></p>
          <p>Visiting Charge (₹100) is already included. Only add charges for parts or extra labor.</p>
          <form onSubmit={handleCompleteGig}>
            <div className="form-group">
              <label htmlFor="additionalAmount">Additional Charges (₹)</label>
              <input
                type="number"
                id="additionalAmount"
                placeholder="e.g., 400 (for parts, etc.)"
                value={additionalAmount}
                onChange={(e) => setAdditionalAmount(e.target.value)}
                autoFocus
              />
            </div>
            {error && <p className="error-message">{error}</p>}
            <div className="invoice-modal-actions">
              <button 
                type="button" 
                className="cancel-button" 
                onClick={() => setShowInvoiceModalFor(null)}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                className="approve-button"
                disabled={loading}
              >
                {loading ? 'Submitting...' : 'Submit Final Invoice'}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // ✅ NEW: Main function to render the active gig card based on status
  const renderActiveGig = () => {
    if (!activeGig) return null;

    // INCOMING REQUEST
    if (activeGig.status === 'REQUESTED') {
      return (
        <div className="portal-card gig-request-card">
          <h3>INCOMING GIG REQUEST!</h3>
          <p>From: <strong>{activeGig.consumerName}</strong><br/>
             Service: <strong>{activeGig.skill}</strong><br/>
             Address: <strong>{activeGig.consumerAddress}</strong>
          </p>
          <button 
            className="check-in-button" 
            onClick={() => handleAcceptGig(activeGig.id)} 
            disabled={loading}
          >
            {loading ? 'Accepting...' : 'Accept Gig (₹100 Visiting Charge)'}
          </button>
        </div>
      );
    }

    // ON THE WAY
    if (activeGig.status === 'ACCEPTED') {
      return (
        <div className="portal-card active-gig-card status-accepted">
          <h3>Gig Accepted!</h3>
          <p>
            For: <strong>{activeGig.consumerName}</strong><br/>
            Address: <strong>{activeGig.consumerAddress}</strong>
          </p>
          <button 
            className="check-in-button" 
            onClick={() => handleStartWork(activeGig.id)} 
            disabled={loading}
          >
            {loading ? '...' : 'I Have Arrived / Start Work'}
          </button>
        </div>
      );
    }
    
    // WORK IN PROGRESS
    if (activeGig.status === 'IN_PROGRESS') {
      return (
        <div className="portal-card active-gig-card status-in-progress">
          <h3>Work In Progress...</h3>
          <p>
            For: <strong>{activeGig.consumerName}</strong><br/>
            Service: <strong>{activeGig.skill}</strong>
          </p>
          <button 
            className="check-out-button" 
            onClick={() => {
              setError('');
              setAdditionalAmount('');
              setShowInvoiceModalFor(activeGig);
            }}
            disabled={loading}
          >
            {loading ? '...' : 'Complete Gig & Add Invoice'}
          </button>
        </div>
      );
    }

    // PENDING PAYMENT
    if (activeGig.status === 'PENDING_PAYMENT') {
      return (
        <div className="portal-card active-gig-card status-pending-payment">
          <h3>Invoice Submitted</h3>
          <p>
            Waiting for <strong>{activeGig.consumerName}</strong> to pay the final amount.
          </p>
          <p className="gig-total-label">FINAL INVOICE</p>
          <p className="gig-total-final">₹{activeGig.totalAmount.toFixed(2)}</p>
          <button 
            className="approve-button" 
            onClick={() => handlePaidByCash(activeGig.id)}
            disabled={loading}
          >
            {loading ? '...' : 'Mark as Paid (Cash Received)'}
          </button>
        </div>
      );
    }

    return null;
  };

  return (
    <div className="labor-portal">
      {renderInvoiceModal()}
      <h2>Labor Dashboard</h2>
      {error && <p className="error-message" style={{textAlign: 'center'}}>{error}</p>}
      
      {/* ✅ This single function now handles the entire active gig flow */}
      {!activeLog && renderActiveGig()}
      
      {/* "GO ONLINE" TOGGLE CARD */}
      {/* ✅ Hide toggle if you have any active work (contract OR gig) */}
      {!activeLog && !activeGig && (
        <div className="availability-toggle">
          <h3>Available for Gigs?</h3>
          <label className="toggle-switch">
            <input 
              type="checkbox" 
              checked={isAvailable}
              onChange={handleAvailabilityToggle}
            />
            <span className="toggle-slider"></span>
          </label>
        </div>
      )}

      {/* "ON THE CLOCK" CARD (Contract Job) */}
      {activeLog && (
        <div className="portal-card">
          <h3>You are currently ON THE CLOCK</h3>
          <p>
            Project: <strong>{activeLog.projectName}</strong><br/>
            Checked In: {new Date(activeLog.checkInTime).toLocaleTimeString()}
          </p>
          <button 
            className="check-out-button" 
            onClick={handleCheckOut} 
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Click to CHECK OUT'}
          </button>
        </div>
      )}

      <div className="portal-grid">
        <div className="portal-card">
          <h3>My Assigned Projects</h3>
          <ul className="job-list">
            {loading && assignments.length === 0 && <p>Loading...</p>}
            {!loading && assignments.length === 0 && (
              <p>You have no assigned projects.</p>
            )}
            {assignments.map(assg => (
              <li key={assg.id} className="job-item">
                <div className="job-item-header">
                  <h4>{assg.projectName}</h4>
                  <button 
                    className="check-in-button" 
                    onClick={() => handleCheckIn(assg.projectId)}
                    disabled={loading || activeLog || activeGig}
                  >
                    Check-In
                  </button>
                </div>
                <p>{assg.projectAddress}</p>
                <p>My Wage: ₹{assg.wageRate} ({assg.wageType.toLowerCase()})</p>
              </li>
            ))
            }
          </ul>
        </div>

        <div className="portal-card">
          {/* ✅ Show map only if no active gig */}
          {!activeGig && (
            <>
              <h3>My Location & Work Ledger</h3>
              <MapContainer center={mapCenter} zoom={13} scrollWheelZoom={true}>
                <TileLayer
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                <MyLocationMarker setMyLatLon={setMyLatLon} />
                {myLatLon && <Marker position={myLatLon}><Popup>You are here</Popup></Marker>}
                {assignments.map(assg => (
                  <Marker 
                    key={assg.id} 
                    position={[assg.projectLatitude, assg.projectLongitude]}
                  >
                    <Popup>{assg.projectName} (Worksite)</Popup>
                  </Marker>
                ))}
              </MapContainer>
            </>
          )}
          
          <h3 style={{marginTop: activeGig ? '0' : '2rem'}}>My Gig History</h3>
          <ul className="ledger-list">
            {loading && completedGigs.length === 0 && <p>Loading history...</p>}
            {!loading && completedGigs.length === 0 && <p>You have no completed gigs.</p>}
            {completedGigs.map(gig => (
              <li key={gig.id} className="ledger-item">
                <div>
                  <span className="status status-COMPLETED">COMPLETED</span>
                  <h4 style={{marginTop: '0.5rem'}}>Gig with {gig.consumerName} ({gig.skill})</h4>
                  <p><strong>Date:</strong> {new Date(gig.paidAt).toLocaleDateString()}</p>
                  <p><strong>Paid:</strong> ₹{gig.totalAmount.toFixed(2)} ({gig.paymentMethod})</p>
                </div>
              </li>
            ))}
          </ul>

          <h3 style={{marginTop: '2rem'}}>My Work Ledger (Contracts)</h3>
          <ul className="ledger-list">
            {loading && logs.length === 0 && <p>Loading...</p>}
            {!loading && logs.length === 0 && <p>You have no work logs yet.</p>}
            {logs.map(log => (
              <li key={log.id} className="ledger-item">
                <span className={`status status-${log.status}`}>{log.status.replace('_', ' ')}</span>
                <h4 style={{marginTop: '0.5rem'}}>{log.projectName}</h4>
                <p><strong>Date:</strong> {new Date(log.checkInTime).toLocaleDateString()}</p>
                {log.wageEarned && (
                  <p><strong>Wage Earned: ₹{log.wageEarned.toFixed(2)}</strong></p>
                )}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
};

export default LaborPortal;