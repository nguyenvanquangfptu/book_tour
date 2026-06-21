import React from 'react';
import '../styles/pages.css';

const RefundPolicyPage: React.FC = () => {
  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>Refund Policy</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>We want you to be completely satisfied with your booking. Please review our refund policy below.</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>Cancellations by Customer</h3>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>More than 30 days before departure: 100% refund (minus transaction fees).</li>
            <li>15 to 30 days before departure: 50% refund.</li>
            <li>Less than 15 days before departure: No refund.</li>
          </ul>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>Cancellations by BookingTour</h3>
          <p>If we have to cancel a tour due to weather or unforeseen circumstances, you will receive a full 100% refund or the option to reschedule your tour at no extra cost.</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>How to Request a Refund</h3>
          <p>Please contact our support team at vanquangqn28@gmail.com with your booking ID and reason for cancellation. Refunds take 5-7 business days to process.</p>
        </div>
      </div>
    </div>
  );
};

export default RefundPolicyPage;
