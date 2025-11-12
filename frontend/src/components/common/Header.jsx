import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext'; // Import the useAuth hook
import './Header.css';

const Header = () => {
  const { user, logout } = useAuth(); // Use the hook
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login'); // Redirect to login page after logout
  };

  // Helper to make the role name look nice
  const getRoleName = (role) => {
    if (role === 'ROLE_LABOR') return 'Labor Portal';
    if (role === 'ROLE_CONTRACTOR') return 'Contractor Portal';
    if (role === 'ROLE_CONSUMER') return 'Consumer Portal';
    return 'Pehchaan';
  };

  return (
    <header className="app-header">
      <div className="header-content">
        <Link to="/" className="header-logo">
          Pehchaan
        </Link>
        <div className="header-nav">
          {user && (
            <span className="user-role">{getRoleName(user.role)}</span>
          )}
          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;