import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import profileService from '../services/profileService';
import './AuthForm.css'; // Reuse our new professional styles

const CompleteProfile = () => {
  const { user, setUser } = useAuth(); 
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [skills, setSkills] = useState(new Set());
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const skillOptions = ['PLUMBER', 'ELECTRICIAN', 'CARPENTER', 'PAINTER', 'MASON'];

  const handleSkillChange = (skill) => {
    const newSkills = new Set(skills);
    if (newSkills.has(skill)) {
      newSkills.delete(skill);
    } else {
      newSkills.add(skill);
    }
    setSkills(newSkills);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    const profileData = {
      firstName,
      lastName,
      skills: Array.from(skills), 
    };

    try {
      const response = await profileService.updateMyProfile(profileData);
      setUser(response.data); // Update the global user state
      navigate('/'); // Navigate to dashboard on success
    } catch (err) {
      setError('Profile update failed. Please try again.');
      console.error(err);
    }
  };

  return (
    <div className="auth-layout">
      {/* 1. The Left-Side Graphic */}
      <div className="auth-graphic">
        <div className="auth-graphic-logo">Pehchaan</div>
        <p className="auth-graphic-subtitle">
          Just one more step. Let's build your public profile so you can get to work.
        </p>
      </div>

      {/* 2. The Right-Side Form Content */}
      <div className="auth-content">
        <form onSubmit={handleSubmit} className="auth-form">
          <h2>Complete Your Profile</h2>
          <p>Welcome! Please fill out your details to continue.</p>
          {error && <p className="error-message">{error}</p>}
          
          <div className="form-group">
            <label htmlFor="firstName">First Name</label>
            <input
              type="text"
              id="firstName"
              autoComplete="given-name"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="lastName">Last Name</label>
            <input
              type="text"
              id="lastName"
              autoComplete="family-name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
          
          {user && user.role === 'ROLE_LABOR' && (
            <div className="form-group">
              <label>Your Skills (Select all that apply)</label>
              <div className="skill-checkbox-group">
                {skillOptions.map(skill => (
                  <label key={skill} className="skill-label">
                    <input
                      type="checkbox"
                      checked={skills.has(skill)}
                      onChange={() => handleSkillChange(skill)}
                    />
                    {/* Simple title case for display */}
                    {skill.charAt(0) + skill.slice(1).toLowerCase()}
                  </label>
                ))}
              </div>
            </div>
          )}
          
          <button type="submit" className="auth-button">Save Profile</button>
        </form>
      </div>
    </div>
  );
};

export default CompleteProfile;