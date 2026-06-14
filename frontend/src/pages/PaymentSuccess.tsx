import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { FaCheckCircle, FaSpinner } from 'react-icons/fa';
import api from '../api/axiosConfig';

const PaymentSuccess: React.FC = () => {
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
            setVerificationError("Xác thực thanh toán thất bại.");
          }
        })
        .catch((error) => {
          console.error("Lỗi xác thực:", error);
          setIsVerifying(false);
          setVerificationError("Lỗi kết nối khi xác thực thanh toán.");
        });
    } else {
      setIsVerifying(false);
    }
  }, [searchParams]);

  if (isVerifying) {
    return (
      <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
        <FaSpinner className="fa-spin" style={{ fontSize: '3rem', color: '#16a34a' }} />
        <h3 style={{ marginTop: '20px', color: '#4b5563' }}>Đang xác thực thanh toán...</h3>
      </div>
    );
  }

  return (
    <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
      <div style={{ background: '#dcfce7', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
        <FaCheckCircle style={{ fontSize: '5rem', color: '#16a34a', marginBottom: '20px' }} />
        <h2 style={{ color: '#16a34a', marginBottom: '15px' }}>Thanh toán thành công!</h2>
        
        {verificationError ? (
          <p style={{ marginBottom: '20px', color: '#dc2626' }}>{verificationError}</p>
        ) : (
          <p style={{ marginBottom: '20px', color: '#4b5563' }}>Cảm ơn bạn đã đặt tour. Giao dịch qua PayOS đã hoàn tất và hệ thống đã ghi nhận thanh toán của bạn.</p>
        )}
        
        <div style={{ marginTop: '30px' }}>
          <Link to="/profile" className="btn btn-primary" style={{ marginRight: '10px' }}>Xem Đơn Đặt</Link>
          <Link to="/" className="btn btn-outline">Về Trang Chủ</Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
