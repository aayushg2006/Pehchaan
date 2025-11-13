import api from './api'; // Import our main Axios instance

/**
 * Fetches the profile for the currently logged-in user.
 */
const getMyProfile = () => {
  return api.get('/api/profile/me');
};

/**
 * Updates the profile for the currently logged-in user.
 * @param {object} profileData - An object containing firstName, lastName, and skills
 */
const updateMyProfile = (profileData) => {
  return api.put('/api/profile/me', profileData);
};

/**
 * ✅ ADDED: Updates the user's availability status.
 * @param {string} status - e.g., "AVAILABLE" or "OFFLINE"
 */
const updateMyStatus = (status) => {
  return api.put('/api/profile/me/status', { status });
};

/**
 * ✅ ADDED: Updates the user's current GPS location.
 * @param {number} latitude 
 * @param {number} longitude 
 */
const updateMyLocation = (latitude, longitude) => {
  return api.put('/api/profile/me/location', { latitude, longitude });
};

const profileService = {
  getMyProfile,
  updateMyProfile,
  updateMyStatus,    // ✅ EXPORT
  updateMyLocation,  // ✅ EXPORT
};

export default profileService;