import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaTimesCircle } from 'react-icons/fa';

const PaymentCancel: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="container" style={{ textAlign: 'center', padding: '100px 20px', minHeight: '60vh' }}>
      <div style={{ background: '#fee2e2', padding: '40px', borderRadius: '10px', maxWidth: '500px', margin: '0 auto' }}>
        <FaTimesCircle style={{ fontSize: '5rem', color: '#dc2626', marginBottom: '20px' }} />
        <h2 style={{ color: '#dc2626', marginBottom: '15px' }}>Thanh toán bị hủy!</h2>
        <p style={{ marginBottom: '20px', color: '#4b5563' }}>Giao dịch thanh toán PayOS đã bị hủy bỏ. Đơn đặt tour của bạn chưa được thanh toán thành công.</p>
        <div style={{ marginTop: '30px' }}>
          <button onClick={() => navigate('/tours')} className="btn btn-primary" style={{ marginRight: '10px' }}>Khám Phá Tour Khác</button>
          <Link to="/" className="btn btn-outline">Về Trang Chủ</Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;
