import api from './api'; // Import our main Axios instance

/**
 * Fetches the profile for the currently logged-in user.
 * The token is automatically added by the api.js interceptor.
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

const profileService = {
  getMyProfile,
  updateMyProfile,
};

export default profileService;