import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { FaCheckCircle, FaSpinner } from 'react-icons/fa';
import api from '../api/axiosConfig';
import { useTranslation } from 'react-i18next';

const PaymentSuccess: React.FC = () => {
  const { t } = useTranslation();
  const [searchParams] = useSearchParams();
  const [isVerifying, setIsVerifying] = useState(true);
  const [verificationError, setVerificationError] = useState<string | null>(null);

  useEffect(() => {
    const orderCode = searchParams.get('orderCode');
    const status = searchParams.get('status');

    if (orderCode && status === 'PAID') {
      // Verify with backend
      api.get(`/payment/payos_transfer_handler/verify?orderCode=${orderCode}`)
        .then((response: any) => {
          if (response.success === "true" || response.status === "PAID") {
            setIsVerifying(false);
          } else {
            setIsVerifying(false);
            setVerificationError(t('payment.verifyFail'));
          }
        })
        .catch((error) => {
          console.error("Lỗi xác thực:", error);
          setIsVerifying(false);
          setVerificationError(t('payment.verifyError'));
        });
    } else {
      setIsVerifying(false);
    }
  }, [searchParams]);

  if (isVerifying) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
        <FaSpinner className="fa-spin" style={{ fontSize: '3rem', color: '#16a34a' }} />
        <h3 style={{ marginTop: '20px', color: '#4b5563' }}>{t('payment.verifying')}</h3>
      </div>
    );
  }

  return (
    <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
      <div style={{ background: '#dcfce7', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
        <FaCheckCircle style={{ fontSize: '5rem', color: '#16a34a', marginBottom: '20px' }} />
        <h2 style={{ color: '#16a34a', marginBottom: '15px' }}>{t('payment.successTitle')}</h2>
        
        {verificationError ? (
          <p style={{ marginBottom: '20px', color: '#dc2626' }}>{verificationError}</p>
        ) : (
          <p style={{ marginBottom: '20px', color: '#4b5563' }}>{t('payment.successText')}</p>
        )}
        
        <div style={{ marginTop: '30px' }}>
          <Link to="/profile" state={{ tab: 'bookings' }} className="btn btn-primary" style={{ marginRight: '10px' }}>{t('payment.viewBooking')}</Link>
          <Link to="/" className="btn btn-outline">{t('payment.home')}</Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
