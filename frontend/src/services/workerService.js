import api from './api';

/**
 * Finds nearby available workers.
 * @param {string} skill 
 * @param {number} lat 
 * @param {number} lon 
 */
const findNearbyWorkers = (skill, lat, lon) => {
  return api.get(`/api/workers/nearby?skill=${skill}&lat=${lat}&lon=${lon}`);
};

const workerService = {
  findNearbyWorkers,
};

export default workerService;