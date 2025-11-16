import React, { useState, useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import workerService from '../../services/workerService';
import gigService from '../../services/gigService';
import './ConsumerPortal.css'; // Make sure this is imported

// (Icon fix... unchanged)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// (GetConsumerLocation... unchanged)
function GetConsumerLocation({ setConsumerLocation, map }) {
  useEffect(() => {
    if (map) {
      map.locate().on("locationfound", function (e) {
        setConsumerLocation(e.latlng);
        map.flyTo(e.latlng, 14);
        L.marker(e.latlng, { title: "Your Location" }).addTo(map)
          .bindPopup("Your Location")
          .openPopup();
      });
    }
  }, [map, setConsumerLocation]);
  return null;
}

const ConsumerPortal = () => {
  const [map, setMap] = useState(null);
  const [consumerLocation, setConsumerLocation] = useState(null);
  const [searchSkill, setSearchSkill] = useState('PLUMBER');
  const [address, setAddress] = useState(''); // ✅ NEW: Address state
  const [nearbyWorkers, setNearbyWorkers] = useState([]);
  const [gigs, setGigs] = useState([]); // ✅ NEW: Gig history
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [requestLoading, setRequestLoading] = useState(false); // For gig request button

  // ✅ NEW: Find the single active gig
  const activeGig = useMemo(() => {
    return gigs.find(g => ['REQUESTED', 'ACCEPTED', 'IN_PROGRESS', 'PENDING_PAYMENT'].includes(g.status));
  }, [gigs]);

  // ✅ NEW: Filter completed gigs
  const completedGigs = useMemo(() => {
    return gigs.filter(g => g.status === 'COMPLETED');
  }, [gigs]);

  // ✅ NEW: Fetch Gigs on load
  useEffect(() => {
    const fetchGigs = async () => {
      try {
        const response = await gigService.getMyGigs();
        setGigs(response.data);
      } catch (err) {
        console.error("Failed to fetch gigs", err);
        setError("Failed to load your gig history.");
      }
    };
    fetchGigs();
  }, []);

  const handleSearch = async () => {
    if (!consumerLocation) {
      setError("Please allow location access to find workers.");
      return;
    }
    setLoading(true);
    setError('');
    try {
      const response = await workerService.findNearbyWorkers(
        searchSkill,
        consumerLocation.lat,
        consumerLocation.lng
      );
      setNearbyWorkers(response.data);
    } catch (err) {
      setError("Failed to find workers. Please try again.");
    }
    setLoading(false);
  };
  
  const handleRequestGig = async (laborerId) => {
    if (!address.trim()) { // ✅ Check for address
      setError("Please enter your full address before requesting.");
      return;
    }
    setRequestLoading(true);
    setError('');
    try {
      const requestData = {
        laborerId,
        skill: searchSkill,
        latitude: consumerLocation.lat,
        longitude: consumerLocation.lng,
        address: address, // ✅ Pass the address
      };
      const response = await gigService.requestGig(requestData);
      setGigs([response.data, ...gigs]); // Add new gig to state
      setNearbyWorkers([]); // Hide other workers
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send request.');
    }
    setRequestLoading(false);
  };

  const handlePayment = async (gigId, paymentMethod) => {
    setLoading(true);
    setError('');
    try {
      // Here you would trigger Razorpay if "ONLINE"
      // For now, we just mark it as paid.
      const response = await gigService.markAsPaid(gigId, paymentMethod);
      // Update the gig in our list
      setGigs(gigs.map(g => g.id === gigId ? response.data : g));
    } catch (err) {
      setError(err.response?.data?.message || 'Payment failed.');
    }
    setLoading(false);
  };

  // ✅ NEW: Render function for the active gig status
  const renderActiveGig = () => {
    if (!activeGig) return null;

    switch (activeGig.status) {
      case 'REQUESTED':
        return (
          <div className="portal-card active-gig-card status-requested">
            <h3>Request Sent!</h3>
            <p>Waiting for <strong>{activeGig.laborerName}</strong> to accept your request for a {activeGig.skill}.</p>
          </div>
        );
      case 'ACCEPTED':
        return (
          <div className="portal-card active-gig-card status-accepted">
            <h3>Laborer On The Way!</h3>
            <p><strong>{activeGig.laborerName}</strong> is on the way to <strong>{activeGig.consumerAddress}</strong>.</p>
            <p className="gig-total">Mandatory Visiting Charge: <strong>₹{activeGig.visitingCharge.toFixed(2)}</strong></p>
          </div>
        );
      case 'IN_PROGRESS':
        return (
          <div className="portal-card active-gig-card status-in-progress">
            <h3>Work In Progress</h3>
            <p><strong>{activeGig.laborerName}</strong> is currently working at your location.</p>
            <p className="gig-total">Current Total: <strong>₹{activeGig.totalAmount.toFixed(2)}</strong></p>
          </div>
        );
      case 'PENDING_PAYMENT':
        return (
          <div className="portal-card active-gig-card status-pending-payment">
            <h3>Work Complete! Please Pay</h3>
            <p><strong>{activeGig.laborerName}</strong> has completed the job.</p>
            <p className="gig-total-label">FINAL INVOICE AMOUNT</p>
            <p className="gig-total-final">₹{activeGig.totalAmount.toFixed(2)}</p>
            <div className="payment-buttons">
              <button 
                className="auth-button" 
                onClick={() => handlePayment(activeGig.id, 'ONLINE')}
                disabled={loading}
              >
                {loading ? 'Processing...' : 'Pay Online (Razorpay)'}
              </button>
              <button 
                className="cancel-button" 
                onClick={() => handlePayment(activeGig.id, 'CASH')}
                disabled={loading}
              >
                {loading ? '...' : 'I Have Paid in Cash'}
              </button>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="consumer-portal">
      <h2>Find a Verified Worker</h2>
      
      {error && <p className="error-message">{error}</p>}
      
      {/* ✅ If there is an active gig, show its status. If not, show search controls. */}
      {activeGig ? renderActiveGig() : (
        <div className="search-controls portal-card">
          <div className="form-group">
            <label>I need a:</label>
            <select value={searchSkill} onChange={(e) => setSearchSkill(e.target.value)}>
              <option value="PLUMBER">Plumber</option>
              <option value="ELECTRICIAN">Electrician</option>
              <option value="CARPENTER">Carpenter</option>
              <option value="PAINTER">Painter</option>
              <option value="MASON">Mason</option>
            </select>
          </div>
          {/* ✅ NEW: Address Input */}
          <div className="form-group" style={{flexGrow: 1}}>
            <label>My Address (for the laborer)</label>
            <input 
              type="text" 
              placeholder="e.g., Flat 101, A Wing, Evershine Gardens"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
            />
          </div>
          <button onClick={handleSearch} className="auth-button" disabled={loading || !consumerLocation}>
            {loading ? '...' : 'Find Nearby'}
          </button>
        </div>
      )}

      {/* ✅ Only show map if there is NO active gig */}
      {!activeGig && (
        <MapContainer 
          center={[19.0760, 72.8777]}
          zoom={13} 
          scrollWheelZoom={true}
          ref={setMap}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <GetConsumerLocation setConsumerLocation={setConsumerLocation} map={map} />

          {nearbyWorkers.map(worker => (
            <Marker 
              key={worker.id}
              position={[worker.latitude, worker.longitude]}
            >
              <Popup>
                <strong>{worker.firstName} {worker.lastName}</strong><br/>
                Skills: {worker.skills.join(', ')}<br/>
                Rating: {worker.rating || 'New'}<br/>
                <button 
                  className="check-in-button" 
                  style={{marginTop: '10px', width: '100%'}}
                  onClick={() => handleRequestGig(worker.id)}
                  disabled={requestLoading}
                >
                  {requestLoading ? 'Sending...' : 'Request Service'}
                </button>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      )}

      {/* ✅ NEW: Gig History Section */}
      <div className="portal-card gig-history">
        <h3>My Gig History</h3>
        {completedGigs.length === 0 && <p>You have no completed gigs.</p>}
        <ul className="ledger-list">
          {completedGigs.map(gig => (
            <li key={gig.id} className="ledger-item">
              <div>
                <span className="status status-COMPLETED">COMPLETED</span>
                <h4 style={{marginTop: '0.5rem'}}>Gig with {gig.laborerName} ({gig.skill})</h4>
                <p><strong>Date:</strong> {new Date(gig.paidAt).toLocaleDateString()}</p>
                <p><strong>Paid:</strong> ₹{gig.totalAmount.toFixed(2)} ({gig.paymentMethod})</p>
              </div>
              <button className="auth-button" disabled>Rate</button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default ConsumerPortal;