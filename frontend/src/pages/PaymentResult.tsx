import React, { useEffect, useState } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { FaCheckCircle, FaTimesCircle } from 'react-icons/fa';
import api from '../api/axiosConfig';

const PaymentResult: React.FC = () => {
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
          <h2>Đang xử lý kết quả thanh toán...</h2>
          <p>Vui lòng không đóng trình duyệt!</p>
        </div>
      )}
      
      {status === 'SUCCESS' && (
        <div style={{ background: '#dcfce7', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
          <FaCheckCircle style={{ fontSize: '5rem', color: '#16a34a', marginBottom: '20px' }} />
          <h2 style={{ color: '#16a34a' }}>Thanh toán thành công!</h2>
          <p>Cảm ơn bạn đã đặt tour. Chúng tôi sẽ liên hệ với bạn trong thời gian sớm nhất.</p>
          <div style={{ marginTop: '30px' }}>
            <Link to="/profile" className="btn btn-primary" style={{ marginRight: '10px' }}>Xem Đơn Đặt</Link>
            <Link to="/" className="btn btn-outline">Về Trang Chủ</Link>
          </div>
        </div>
      )}

      {status === 'FAILED' && (
        <div style={{ background: '#fee2e2', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
          <FaTimesCircle style={{ fontSize: '5rem', color: '#dc2626', marginBottom: '20px' }} />
          <h2 style={{ color: '#dc2626' }}>Thanh toán thất bại!</h2>
          <p>Giao dịch đã bị hủy hoặc xảy ra lỗi. Vui lòng thử lại sau.</p>
          <div style={{ marginTop: '30px' }}>
            <button onClick={() => navigate(-1)} className="btn btn-primary">Thử lại</button>
            <Link to="/" className="btn btn-outline" style={{ marginLeft: '10px' }}>Về Trang Chủ</Link>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentResult;
