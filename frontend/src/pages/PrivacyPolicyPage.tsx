import React from 'react';
import '../styles/pages.css';

const PrivacyPolicyPage: React.FC = () => {
  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>Privacy Policy</h1>
        
        <div style={{ marginBottom: '24px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
          <p>At BookingTour, we take your privacy seriously. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you visit our website.</p>
          
          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>Information We Collect</h3>
          <p>We may collect personal identification information from Users in a variety of ways, including, but not limited to, when Users visit our site, register on the site, place an order, and in connection with other activities, services, features or resources we make available on our Site. Users may be asked for, as appropriate, name, email address, mailing address, phone number.</p>

          <h3 style={{ marginTop: '24px', marginBottom: '12px', color: 'var(--text-primary)' }}>How We Use Collected Information</h3>
          <p>BookingTour may collect and use Users personal information for the following purposes:</p>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>To improve customer service</li>
            <li>To personalize user experience</li>
            <li>To process payments</li>
            <li>To send periodic emails regarding their order</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicyPage;
