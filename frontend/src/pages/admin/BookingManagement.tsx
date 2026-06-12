import React, { useState, useEffect } from 'react';
import { FaCheckCircle, FaTimesCircle, FaEye } from 'react-icons/fa';
import api from '../../api/axiosConfig';

interface Booking {
  id: number;
  userId: number;
  tourId: number;
  tourName: string;
  customerName: string;
  numberOfPeople: number;
  totalPrice: number;
  status: string;
  bookingDate: string;
}

const BookingManagement: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchBookings = async () => {
    try {
      const response = await api.get('/bookings'); // Admin endpoint fetches all
      if (response.data && response.data.data) {
        setBookings(response.data.data);
      }
    } catch (error) {
      console.error('Failed to fetch bookings', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleConfirm = async (id: number) => {
    if (window.confirm('Xác nhận duyệt đơn hàng này?')) {
      try {
        await api.put(`/bookings/${id}/confirm`);
        fetchBookings();
      } catch (error) {
        console.error('Failed to confirm booking', error);
        alert('Có lỗi xảy ra khi xác nhận đơn hàng.');
      }
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <span style={{ background: '#fef08a', color: '#ca8a04', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>Chờ Duyệt</span>;
      case 'CONFIRMED':
        return <span style={{ background: '#bfdbfe', color: '#2563eb', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>Đã Xác Nhận</span>;
      case 'PAID':
        return <span style={{ background: '#dcfce7', color: '#16a34a', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>Đã Thanh Toán</span>;
      case 'CANCELLED':
        return <span style={{ background: '#fecaca', color: '#dc2626', padding: '4px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}>Đã Hủy</span>;
      default:
        return <span>{status}</span>;
    }
  };

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>Quản lý Đơn Đặt Tour</h2>
      </div>

      {loading ? (
        <p>Đang tải dữ liệu...</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Mã Đơn</th>
              <th>Ngày Đặt</th>
              <th>Khách Hàng</th>
              <th>Tour</th>
              <th>Số Lượng</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            {bookings.length > 0 ? bookings.map(b => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td>{new Date(b.bookingDate).toLocaleDateString('vi-VN')}</td>
                <td>{b.customerName || `User #${b.userId}`}</td>
                <td>{b.tourName || `Tour #${b.tourId}`}</td>
                <td>{b.numberOfPeople}</td>
                <td>{b.totalPrice.toLocaleString()} VNĐ</td>
                <td>{getStatusBadge(b.status)}</td>
                <td>
                  <div className="action-btns">
                    {b.status === 'PENDING' && (
                      <button 
                        onClick={() => handleConfirm(b.id)}
                        style={{ background: '#16a34a', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px' }}
                      >
                        <FaCheckCircle /> Duyệt
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            )) : (
              <tr>
                <td colSpan={8} style={{ textAlign: 'center' }}>Chưa có đơn hàng nào</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default BookingManagement;
