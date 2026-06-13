import React from 'react';
import '../styles/pages.css';

const FAQPage: React.FC = () => {
  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>Frequently Asked Questions</h1>
        
        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>1. How do I book a tour?</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>You can browse our available tours, select your preferred date and number of people, and click "Book Now" or add it to your cart to check out later.</p>
        </div>

        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>2. What payment methods are accepted?</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>We currently accept VNPay and bank transfers. All payments are securely processed.</p>
        </div>

        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>3. Can I cancel my booking?</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>Yes, cancellations are allowed subject to our Refund Policy. Please review the specific terms of your tour for cancellation deadlines.</p>
        </div>
      </div>
    </div>
  );
};

export default FAQPage;
