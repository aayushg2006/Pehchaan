import React, { useState, useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet'; 
import { useAuth } from '../../context/AuthContext'; // ✅ Get useAuth
import assignmentService from '../../services/assignmentService'; 
import workLogService from '../../services/workLogService';
import profileService from '../../services/profileService'; // ✅ Import profileService
import './LaborPortal.css'; 

// Helper to show laborer's location
function MyLocationMarker({ setMyLatLon }) {
  const map = useMap();
  useEffect(() => {
    map.locate({ setView: true, maxZoom: 16, watch: true }); // Watch for location changes

    function onLocationFound(e) {
      setMyLatLon(e.latlng);
      // We'll just set the location, not add a marker here
      // to avoid duplicates
    }

    map.on('locationfound', onLocationFound);
    return () => map.off('locationfound', onLocationFound); // Cleanup
  }, [map, setMyLatLon]);

  return null;
}

// Fix for default Leaflet icon
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});


const LaborPortal = () => {
  const { user, setUser } = useAuth(); // ✅ Get setUser to update global state
  const [assignments, setAssignments] = useState([]);
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [myLatLon, setMyLatLon] = useState(null); 
  
  // ✅ NEW: State for the toggle, initialized from the user object
  const [isAvailable, setIsAvailable] = useState(user.status === 'AVAILABLE');
  
  const mapCenter = [19.0760, 72.8777]; // Default center

  const activeLog = useMemo(() => {
    return logs.find(log => log.status === 'ACTIVE');
  }, [logs]);

  // Fetch all laborer data
  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const assignmentsPromise = assignmentService.getMyAssignments();
      const logsPromise = workLogService.getMyLogs();
      
      const [assignmentsResponse, logsResponse] = await Promise.all([assignmentsPromise, logsPromise]);
      
      setAssignments(assignmentsResponse.data);
      setLogs(logsResponse.data);
    } catch (err) {
      setError('Failed to load dashboard data.');
      console.error(err);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchData();
  }, []);

  // ✅ NEW: Handler for the "Go Online" toggle
  const handleAvailabilityToggle = async (e) => {
    const newStatus = e.target.checked;
    setIsAvailable(newStatus);
    setError('');
    
    if (newStatus) {
      // Toggle ON: Update location, then set status
      if (!myLatLon) {
        setError("Waiting for your location... please try again in a moment.");
        setIsAvailable(false); // Revert toggle
        return;
      }
      try {
        await profileService.updateMyLocation(myLatLon.lat, myLatLon.lng);
        const response = await profileService.updateMyStatus('AVAILABLE');
        setUser(response.data); // Update global user state
      } catch (err) {
        setError("Failed to go online.");
        setIsAvailable(false);
      }
    } else {
      // Toggle OFF: Just set status
      try {
        const response = await profileService.updateMyStatus('OFFLINE');
        setUser(response.data);
      } catch (err) {
        setError("Failed to go offline.");
        setIsAvailable(true);
      }
    }
  };


  const handleCheckIn = (projectId) => {
    setLoading(true);
    setError('');
    
    if (activeLog) { 
        setError('You must check out from your active job first.');
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
        await fetchData(); // Refresh all data
      } catch (err) {
        setError(err.response?.data?.message || 'Check-in failed. Are you at the worksite?');
        setLoading(false);
      }
    })();
  };

  const handleCheckOut = async () => {
    setLoading(true);
    setError('');
    try {
      await workLogService.checkOut();
      await fetchData(); // Refresh all data
    } catch (err) {
      setError(err.response?.data?.message || 'Check-out failed.');
      setLoading(false);
    }
  };

  return (
    <div className="labor-portal">
      <h2>Labor Dashboard</h2>
      {error && <p className="error-message" style={{textAlign: 'center'}}>{error}</p>}
      
      {/* ✅ NEW: "GO ONLINE" TOGGLE CARD */}
      {/* This only shows if you are NOT on a contract job */}
      {!activeLog && (
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

      {/* "ON THE CLOCK" CARD */}
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
            {loading && <p>Loading...</p>}
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
                    disabled={loading || activeLog} // Disable if on a job
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
          <h3>My Location & Work Ledger</h3>
          
          <MapContainer center={mapCenter} zoom={13} scrollWheelZoom={true}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            <MyLocationMarker setMyLatLon={setMyLatLon} />
            
            {/* Show a marker for our current location */}
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
          
          <h3 style={{marginTop: '2rem'}}>My Work Ledger</h3>
          <ul className="ledger-list">
            {loading && logs.length === 0 && <p>Loading...</p>}
            {!loading && logs.length === 0 && (
              <p>You have no work logs yet.</p>
            )}
            {logs.map(log => (
              <li key={log.id} className="ledger-item">
                <span className={`status status-${log.status}`}>{log.status.replace('_', ' ')}</span>
                <h4 style={{marginTop: '0.5rem'}}>{log.projectName}</h4>
                <p>
                  <strong>Date:</strong> {new Date(log.checkInTime).toLocaleDateString()}
                </p>
                {log.checkOutTime && (
                    <p><strong>Duration:</strong> {
                        (Math.abs(new Date(log.checkOutTime) - new Date(log.checkInTime)) / 36e5).toFixed(2)
                    } hours</p>
                )}
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