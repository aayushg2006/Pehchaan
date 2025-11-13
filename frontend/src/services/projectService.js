import api from './api'; // Import our main Axios instance

/**
 * Fetches all projects for the currently logged-in contractor.
 */
const getMyProjects = () => {
  return api.get('/api/projects/my-projects');
};

/**
 * Creates a new project.
 * @param {object} projectData - { name, address, latitude, longitude }
 */
const createProject = (projectData) => {
  return api.post('/api/projects', projectData);
};

const projectService = {
  getMyProjects,
  createProject,
};

export default projectService;