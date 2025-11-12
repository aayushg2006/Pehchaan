import React from 'react';
import { useAuth } from '../context/AuthContext';
import Header from '../components/common/Header.jsx';

import ContractorPortal from '../components/portals/ContractorPortal.jsx';
import LaborPortal from '../components/portals/LaborPortal.jsx';
import ConsumerPortal from '../components/portals/ConsumerPortal.jsx'; // ✅ ADD

const Dashboard = () => {
  const { user } = useAuth(); 

  const renderPortal = () => {
    switch (user?.role) {
      case 'ROLE_LABOR':
        return <LaborPortal />;
      case 'ROLE_CONTRACTOR':
        return <ContractorPortal />; 
      case 'ROLE_CONSUMER':
        return <ConsumerPortal />; // ✅ RENDER CONSUMER PORTAL
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