import React, { useEffect, useState } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import api from '../api/axiosConfig';
import { useTranslation } from 'react-i18next';

const PaymentResult: React.FC = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'LOADING' | 'SUCCESS' | 'FAILED'>('LOADING');

  useEffect(() => {
    const verifyPayment = async () => {
      if (!location.search) {
        setStatus('FAILED');
        return;
      }
      try {
        // Send the query string exactly as received from PayOS to our backend
        const response = await api.get(`/payments/payos-callback${location.search}`);
        if (response.data && response.data.success) {
          setStatus('SUCCESS');
        } else {
          setStatus('FAILED');
        }
      } catch (error) {
        console.error('Lỗi khi xác minh thanh toán:', error);
        setStatus('FAILED');
      }
    };

    verifyPayment();
  }, [location.search]);

  return (
    <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
      {status === 'LOADING' && (
        <div>
          <h2>{t('payment.processingResult')}</h2>
          <p>{t('payment.dontClose')}</p>
        </div>
      )}
      
      {status === 'SUCCESS' && (
        <div style={{ background: '#dcfce7', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
          <FaCheckCircle style={{ fontSize: '5rem', color: '#16a34a', marginBottom: '20px' }} />
          <h2 style={{ color: '#16a34a' }}>{t('payment.successTitle')}</h2>
          <p>{t('payment.successResultText')}</p>
          <div style={{ marginTop: '30px' }}>
            <Link to="/profile" state={{ tab: 'bookings' }} className="btn btn-primary" style={{ marginRight: '10px' }}>{t('payment.viewBooking')}</Link>
            <Link to="/" className="btn btn-outline">{t('payment.home')}</Link>
          </div>
        </div>
      )}

      {status === 'FAILED' && (
        <div style={{ background: '#fee2e2', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
          <FaTimesCircle style={{ fontSize: '5rem', color: '#dc2626', marginBottom: '20px' }} />
          <h2 style={{ color: '#dc2626' }}>{t('payment.failTitle')}</h2>
          <p>{t('payment.failText')}</p>
          <div style={{ marginTop: '30px' }}>
            <button onClick={() => navigate(-1)} className="btn btn-primary">{t('payment.tryAgain')}</button>
            <Link to="/" className="btn btn-outline" style={{ marginLeft: '10px' }}>{t('payment.home')}</Link>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentResult;
