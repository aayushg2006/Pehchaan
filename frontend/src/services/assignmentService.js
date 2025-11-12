import api from './api';

/**
 * Searches for workers by skill.
 * @param {string} skill 
 */
const searchWorkers = (skill) => {
  return api.get(`/api/assignments/workers/search?skill=${skill}`);
};

/**
 * Assigns a worker to a project with a specific wage.
 * @param {object} assignmentData - { projectId, laborerId, wageRate, wageType }
 */
const createAssignment = (assignmentData) => {
  return api.post('/api/assignments', assignmentData);
};

/**
 * Fetches the assignments for the currently logged-in laborer.
 */
const getMyAssignments = () => {
  return api.get('/api/assignments/my-projects');
};

const assignmentService = {
  searchWorkers,
  createAssignment,
  getMyAssignments,
};

export default assignmentService;