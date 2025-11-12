import React from 'react';
import { useAuth } from '../context/AuthContext';
import Header from '../components/common/Header.jsx';

// We'll create these placeholder components in Phase 2
// import LaborPortal from '../components/portals/LaborPortal.jsx';
// import ContractorPortal from '../components/portals/ContractorPortal.jsx';
// import ConsumerPortal from '../components/portals/ConsumerPortal.jsx';

const Dashboard = () => {
  const { user } = useAuth(); 

  const renderPortal = () => {
    // This is where we will do role-based routing
    switch (user?.role) {
      case 'ROLE_LABOR':
        // return <LaborPortal />;
        return <h2>Welcome, {user.firstName}! (Labor Portal TODO)</h2>;
      case 'ROLE_CONTRACTOR':
        // return <ContractorPortal />;
        return <h2>Welcome, {user.firstName}! (Contractor Portal TODO)</h2>;
      case 'ROLE_CONSUMER':
        // return <ConsumerPortal />;
        return <h2>Welcome, {user.firstName}! (Consumer Portal TODO)</h2>;
      default:
        return <h2>Loading...</h2>;
    }
  };

  return (
    <div className="dashboard-layout">
      <Header />
      <main className="portal-content">
        {renderPortal()}
      </main>
    </div>
  );
};

export default Dashboard;