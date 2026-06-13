import React from 'react';
import '../styles/pages.css';

const TermsOfServicePage: React.FC = () => {
  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>Terms of Service</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>Welcome to BookingTour. By accessing our website, you are agreeing to be bound by these terms of service, all applicable laws and regulations, and agree that you are responsible for compliance with any applicable local laws.</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>1. Use License</h3>
          <p>Permission is granted to temporarily download one copy of the materials (information or software) on BookingTour's website for personal, non-commercial transitory viewing only.</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>2. Booking and Payments</h3>
          <p>When you make a booking, you agree to pay the total cost of the tour including any taxes and fees. We reserve the right to cancel any booking if payment is not received in full.</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>3. Modifications</h3>
          <p>BookingTour may revise these terms of service for its website at any time without notice. By using this website you are agreeing to be bound by the then current version of these terms of service.</p>
        </div>
      </div>
    </div>
  );
};

export default TermsOfServicePage;
