import React, { useState, useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from 'react-leaflet';
import L from 'leaflet';
import projectService from '../../services/projectService';
import workLogService from '../../services/workLogService';
import assignmentService from '../../services/assignmentService'; 
import './ContractorPortal.css'; 

// (Icon fix is unchanged)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// (MapClickHandler is unchanged)
function MapClickHandler({ onLocationSelect }) {
  useMapEvents({
    click(e) {
      onLocationSelect(e.latlng);
    },
  });
  return null;
}

// (SetInitialViewAndPin is unchanged)
function SetInitialViewAndPin({ setLocation, setMapCenter }) {
  const map = useMap(); // Get the map instance

  useEffect(() => {
    map.locate().on("locationfound", function (e) {
      // 1. Set the pin location state (for the form)
      setLocation(e.latlng);
      // 2. Set the map center state (for the MapContainer)
      setMapCenter(e.latlng);
      // 3. Fly the map view to the new location
      map.flyTo(e.latlng, 13);
    }).on("locationerror", function (e) {
      // If user denies location, it just stays at the default (Mumbai)
      console.warn("Could not get user location: ", e.message);
    });
  }, [map, setLocation, setMapCenter]); // Run this effect once

  return null; // This component doesn't render anything
}


const ContractorPortal = () => {
  const [projects, setProjects] = useState([]);
  const [logs, setLogs] = useState([]); 
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false); 
  
  // --- Form state ---
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [location, setLocation] = useState(null); 
  const [mapCenter, setMapCenter] = useState([19.0760, 72.8777]); // Default: Mumbai

  // --- Worker Search State ---
  const [skillSearch, setSkillSearch] = useState('PLUMBER');
  const [searchResults, setSearchResults] = useState([]);
  const [assignmentProjectId, setAssignmentProjectId] = useState('');
  const [assignmentWage, setAssignmentWage] = useState('');
  const [assignmentWageType, setAssignmentWageType] = useState('DAILY');

  // ... (fetchData is unchanged) ...
  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      const projectsPromise = projectService.getMyProjects();
      const logsPromise = workLogService.getMyLogs();
      const [projectsResponse, logsResponse] = await Promise.all([projectsPromise, logsPromise]);
      setProjects(projectsResponse.data);
      setLogs(logsResponse.data);
      if (projectsResponse.data.length > 0) {
        setAssignmentProjectId(projectsResponse.data[0].id); 
      }
    } catch (err) {
      setError('Failed to fetch dashboard data.');
    }
    setLoading(false);
  };
  useEffect(() => {
    fetchData();
  }, []);

  // ... (pendingLogs is unchanged) ...
  const { pendingLogs } = useMemo(() => {
    return logs.reduce((acc, log) => {
      if (log.status === 'PENDING_APPROVAL') acc.pendingLogs.push(log);
      return acc;
    }, { pendingLogs: [] });
  }, [logs]);

  // ... (handleCreateProject is unchanged) ...
  const handleCreateProject = async (e) => {
    e.preventDefault();
    if (!location) { 
        setError('Please select a project location on the map.');
        return;
    }
    setError('');
    const projectData = { name, address, latitude: location.lat, longitude: location.lng };
    try {
      const response = await projectService.createProject(projectData);
      setProjects([response.data, ...projects]); 
      setName('');
      setAddress('');
      setLocation(null);
      if (projects.length === 0) { 
        setAssignmentProjectId(response.data.id);
      }
    } catch (err) {
      setError('Failed to create project. Please try again.');
    }
  };

  // ... (handleApproveLog is unchanged) ...
  const handleApproveLog = async (logId) => {
    setLoading(true);
    setError('');
    try {
      await workLogService.approveLog(logId);
      await fetchData(); 
    } catch (err) {
      setError('Failed to approve log.');
      setLoading(false);
    }
  };

  // ... (handleWorkerSearch is unchanged) ...
  const handleWorkerSearch = async () => {
    if (!skillSearch) return;
    setLoading(true);
    setError('');
    try {
      const response = await assignmentService.searchWorkers(skillSearch);
      setSearchResults(response.data);
    } catch (err) {
      setError('Failed to search workers.');
    }
    setLoading(false);
  };

  // ... (handleAssignWorker is unchanged) ...
  const handleAssignWorker = async (laborerId) => {
    if (!assignmentWage || !assignmentProjectId) {
      setError('Please select a project and set a wage.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const assignmentData = {
        projectId: assignmentProjectId,
        laborerId,
        wageRate: parseFloat(assignmentWage),
        wageType: assignmentWageType
      };
      await assignmentService.createAssignment(assignmentData);
      alert('Worker assigned successfully!');
      setSearchResults([]); 
      setAssignmentWage('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to assign worker.');
    }
    setLoading(false);
  };

  return (
    <div className="contractor-portal">
      <h2>Contractor Dashboard</h2>
      {error && <p className="error-message" style={{textAlign: 'center'}}>{error}</p>}

      {/* --- (Pending Payroll card is unchanged) --- */}
      <div className="portal-card">
        <h3>Pending Payroll Approvals</h3>
        <ul className="ledger-list">
          {loading && logs.length === 0 && <p>Loading logs...</p>}
          {!loading && pendingLogs.length === 0 && (
            <p>You have no pending approvals.</p>
          )}
          {pendingLogs.map(log => (
            <li key={log.id} className="ledger-item">
              <div>
                <span className="status-badge status-PENDING_APPROVAL">PENDING</span>
                <h4>{log.laborerName}</h4>
                <p>Project: {log.projectName}</p>
                <p>Date: {new Date(log.checkInTime).toLocaleDateString()}</p>
                <p className="wage">Wage Due: ₹{log.wageEarned.toFixed(2)}</p>
              </div>
              <button 
                className="approve-button"
                onClick={() => handleApproveLog(log.id)}
                disabled={loading}
              >
                Approve
              </button>
            </li>
          ))}
        </ul>
      </div>
      
      {/* --- (Grid is unchanged) --- */}
      <div className="portal-grid">
        {/* --- (Create Project card is unchanged) --- */}
        <div className="portal-card">
          <h3>Create New Project</h3>
          
          <MapContainer center={mapCenter} zoom={13} scrollWheelZoom={true}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {location && <Marker position={location} />}
            
            <SetInitialViewAndPin setLocation={setLocation} setMapCenter={setMapCenter} />
            <MapClickHandler onLocationSelect={setLocation} />
          </MapContainer>
          
          <form onSubmit={handleCreateProject} className="project-form">
            <div className="form-group">
              <label htmlFor="name">Project Name</label>
              <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label htmlFor="address">Address / Site Details</label>
              <input type="text" id="address" value={address} onChange={(e) => setAddress(e.target.value)} required />
            </div>
            <button type="submit" className="auth-button" disabled={loading}>Create Project</button>
          </form>
        </div>

        {/* --- (My Projects card is unchanged) --- */}
        <div className="portal-card">
          <h3>My Projects</h3>
          <ul className="project-list">
            {loading && projects.length === 0 && <p>Loading projects...</p>}
            {!loading && projects.length === 0 && (
              <p>You have not created any projects yet.</p>
            )}
            {projects.map(project => (
              <li key={project.id} className="project-item">
                <h4>{project.name}</h4>
                <p>{project.address}</p>
              </li>
            ))}
          </ul>
        </div>
      </div>
      
      {/* --- ASSIGN WORKERS --- */}
      <div className="portal-card">
        <h3>Assign Workers to Projects</h3>
        <div className="form-group">
          <label>Search by Skill</label>
          <div style={{display: 'flex', gap: '1rem'}}>
            
            {/* ✅ FIX IS HERE: Removed "e.g." */}
            <select value={skillSearch} onChange={(e) => setSkillSearch(e.target.value)}>
              <option value="PLUMBER">Plumber</option>
              <option value="ELECTRICIAN">Electrician</option>
              <option value="CARPENTER">Carpenter</option>
              <option value="PAINTER">Painter</option>
              <option value="MASON">Mason</option>
            </select>
            
            <button onClick={handleWorkerSearch} disabled={loading} className="auth-button">Search</button>
          </div>
        </div>
        
        {/* --- (Search results list is unchanged) --- */}
        {searchResults.length > 0 && (
          <ul className="ledger-list" style={{marginTop: '1.5rem'}}>
            {searchResults.map(worker => (
              <li key={worker.id} className="ledger-item">
                <div>
                  <h4>{worker.firstName} {worker.lastName}</h4>
                  <p>Rating: {worker.rating || 'N/A'}</p>
                </div>
                <div style={{display: 'flex', flexDirection: 'column', gap: '0.5rem'}}>
                  <select value={assignmentProjectId} onChange={(e) => setAssignmentProjectId(e.target.value)}>
                    {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                  </select>
                  <input type="number" placeholder="Wage Rate" value={assignmentWage} onChange={(e) => setAssignmentWage(e.target.value)} />
                  <select value={assignmentWageType} onChange={(e) => setAssignmentWageType(e.target.value)}>
                      <option value="DAILY">Per Day</option>
                      <option value="HOURLY">Per Hour</option>
                  </select>
                  <button onClick={() => handleAssignWorker(worker.id)} className="check-in-button" disabled={loading}>Assign</button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default ContractorPortal;