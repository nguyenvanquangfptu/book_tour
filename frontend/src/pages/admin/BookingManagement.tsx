import React, { useState, useEffect } from 'react';
import { FaCheckCircle } from 'react-icons/fa';
import api from '../../api/axiosConfig';
import Swal from 'sweetalert2';
import { formatPrice } from '../../utils/formatPrice';

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
  travelDate?: string;
}

const BookingManagement: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');

  const fetchBookings = async () => {
    try {
      const response: any = await api.get('/bookings'); // Admin endpoint fetches all
      if (response.data) {
        setBookings(response.data);
      } else if (Array.isArray(response)) {
        setBookings(response);
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
    const result = await Swal.fire({
      title: 'Xác nhận duyệt đơn hàng?',
      text: "Bạn có chắc chắn muốn duyệt đơn hàng này không?",
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#16a34a',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Duyệt',
      cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
      try {
        await api.put(`/bookings/${id}/confirm`);
        setBookings(bookings.map(b => b.id === id ? { ...b, status: 'CONFIRMED' } : b));
        Swal.fire('Thành công', 'Đơn hàng đã được duyệt', 'success');
      } catch (error) {
        console.error('Failed to confirm booking', error);
        Swal.fire({
          icon: 'error',
          title: 'Lỗi',
          text: 'Có lỗi xảy ra khi xác nhận đơn hàng.',
          confirmButtonColor: '#3b82f6'
        });
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

  const filteredBookings = bookings.filter(b => {
    if (filterStatus === 'ALL') return true;
    return b.status === filterStatus;
  });

  return (
    <div className="admin-panel">
      <div className="admin-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Quản lý Đơn Đặt Tour</h2>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <label style={{ fontWeight: 'bold' }}>Lọc theo trạng thái:</label>
          <select 
            value={filterStatus} 
            onChange={(e) => setFilterStatus(e.target.value)}
            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
          >
            <option value="ALL">Tất cả</option>
            <option value="PENDING">Chờ duyệt</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="PAID">Đã thanh toán</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
      </div>

      {loading ? (
        <p>Đang tải dữ liệu...</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Mã Đơn</th>
              <th>Ngày Đặt</th>
              <th>Ngày Đi (Travel Date)</th>
              <th>Khách Hàng</th>
              <th>Tour</th>
              <th>Số Lượng</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            {filteredBookings.length > 0 ? filteredBookings.map(b => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td>{new Date(b.bookingDate).toLocaleDateString('vi-VN')}</td>
                <td>{b.travelDate ? new Date(b.travelDate).toLocaleDateString('vi-VN') : 'N/A'}</td>
                <td>{b.customerName || `User #${b.userId}`}</td>
                <td>{b.tourName || `Tour #${b.tourId}`}</td>
                <td>{b.numberOfPeople}</td>
                <td>{formatPrice(b.totalPrice)}</td>
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
                <td colSpan={9} style={{ textAlign: 'center' }}>Chưa có đơn hàng nào</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default BookingManagement;
