import api from './api'; // Import our main Axios instance

/**
 * Checks a laborer into a project.
 * @param {object} checkInData - { projectId, latitude, longitude }
 */
const checkIn = (checkInData) => {
  return api.post('/api/work/check-in', checkInData);
};

/**
 * Checks a laborer out of their active project.
 */
const checkOut = () => {
  return api.post('/api/work/check-out');
};

/**
 * Fetches all work logs for the current user (laborer or contractor).
 */
const getMyLogs = () => {
  return api.get('/api/work/my-logs');
};

/**
 * ✅ ADDED: Approves a specific work log.
 * @param {number} logId - The ID of the work log to approve
 */
const approveLog = (logId) => {
  return api.post(`/api/work/logs/${logId}/approve`);
};


const workLogService = {
  checkIn,
  checkOut,
  getMyLogs,
  approveLog, // ✅ EXPORT IT
};

export default workLogService;