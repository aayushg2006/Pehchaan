import api from './api';

/**
 * (Consumer) Requests a new gig from a laborer.
 * @param {object} requestData - { laborerId, skill, latitude, longitude, address }
 */
const requestGig = (requestData) => {
  return api.post('/api/gigs/request', requestData);
};

/**
 * (Laborer) Accepts an incoming gig request.
 * @param {number} gigId - The ID of the gig to accept
 */
const acceptGig = (gigId) => {
  return api.post(`/api/gigs/${gigId}/accept`);
};

/**
 * (Laborer) Marks a gig as in-progress.
 * @param {number} gigId
 */
const startWork = (gigId) => {
  return api.post(`/api/gigs/${gigId}/start`);
};

/**
 * (Laborer) Completes a gig and sets additional invoice charges.
 * @param {number} gigId - The ID of the gig to complete
 * @param {number} additionalAmount - The final *additional* invoice amount (visiting charge is auto-added)
 */
const completeAndInvoice = (gigId, additionalAmount) => {
  // Ensure we send 0 if the amount is empty
  const amount = additionalAmount || 0;
  return api.post(`/api/gigs/${gigId}/complete`, { additionalAmount: amount });
};

/**
 * (Consumer/Laborer) Marks a gig as paid.
 * @param {number} gigId
 * @param {string} paymentMethod - "CASH" or "ONLINE"
 */
const markAsPaid = (gigId, paymentMethod) => {
  return api.post(`/api/gigs/${gigId}/pay`, { paymentMethod });
};

/**
 * (Both) Gets all gigs (past and present) for the logged-in user.
 */
const getMyGigs = () => {
  return api.get('/api/gigs/my-gigs');
};

const gigService = {
  requestGig,
  acceptGig,
  startWork,
  completeAndInvoice,
  markAsPaid,
  getMyGigs,
};

export default gigService;