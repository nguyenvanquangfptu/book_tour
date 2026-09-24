import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaTimesCircle } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

const PaymentCancel: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
      <div style={{ background: '#fee2e2', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
        <FaTimesCircle style={{ fontSize: '5rem', color: '#dc2626', marginBottom: '20px' }} />
        <h2 style={{ color: '#dc2626', marginBottom: '15px' }}>{t('payment.cancelTitle')}</h2>
        <p style={{ marginBottom: '20px', color: '#4b5563' }}>{t('payment.cancelText')}</p>
        <div style={{ marginTop: '30px' }}>
          <button onClick={() => navigate('/tours')} className="btn btn-primary" style={{ marginRight: '10px' }}>{t('payment.exploreOthers')}</button>
          <Link to="/" className="btn btn-outline">{t('payment.home')}</Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;
