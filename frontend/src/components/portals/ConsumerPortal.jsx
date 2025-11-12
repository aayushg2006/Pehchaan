import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import workerService from '../../services/workerService';
import './ConsumerPortal.css'; // Import new styles

// (Icon fix)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Helper to find and center on the consumer's location
function GetConsumerLocation({ setConsumerLocation, map }) {
  useEffect(() => {
    if (map) {
      map.locate().on("locationfound", function (e) {
        setConsumerLocation(e.latlng);
        map.flyTo(e.latlng, 14);
        L.marker(e.latlng, { /* custom icon eventually */ }).addTo(map)
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
  const [nearbyWorkers, setNearbyWorkers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

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

  return (
    <div className="consumer-portal">
      <h2>Find a Verified Worker</h2>
      
      <div className="search-controls">
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
        <button onClick={handleSearch} className="auth-button" disabled={loading || !consumerLocation}>
          {loading ? 'Searching...' : 'Find Nearby'}
        </button>
      </div>

      {error && <p className="error-message">{error}</p>}

      <MapContainer 
        center={[19.0760, 72.8777]} // Default: Mumbai
        zoom={13} 
        scrollWheelZoom={true}
        ref={setMap} // Save the map instance
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <GetConsumerLocation setConsumerLocation={setConsumerLocation} map={map} />

        {/* ✅ FIXED: Map over the results and use worker's real location */}
        {nearbyWorkers.map(worker => (
          <Marker 
            key={worker.id}
            // Use the latitude/longitude from the API response
            position={[worker.latitude, worker.longitude]}
          >
            <Popup>
              <strong>{worker.firstName} {worker.lastName}</strong><br/>
              Skills: {worker.skills.join(', ')}<br/>
              Rating: {worker.rating || 'New'}<br/>
              <button>Request Service</button>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
};

export default ConsumerPortal;