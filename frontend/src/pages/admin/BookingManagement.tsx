import React, { useState, useEffect } from 'react';
import { FaCheckCircle, FaEye, FaMapMarkerAlt, FaCalendarAlt, FaUsers, FaUser, FaPhone, FaEnvelope, FaStickyNote, FaMoneyBillWave, FaMap, FaFileInvoiceDollar, FaIdCard } from 'react-icons/fa';
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
  const [selectedBooking, setSelectedBooking] = useState<any>(null);
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

  const filteredBookings = bookings
    .filter(b => {
      if (filterStatus === 'ALL') return true;
      return b.status === filterStatus;
    })
    .sort((a, b) => b.id - a.id);

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
                  <div className="action-btns" style={{ display: 'flex', gap: '5px' }}>
                    <button 
                      onClick={() => setSelectedBooking(b)}
                      style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px' }}
                    >
                      <FaEye /> Xem chi tiết
                    </button>
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

      {/* Booking Details Modal */}
      {selectedBooking && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, padding: '20px' }} onClick={() => setSelectedBooking(null)}>
          <div style={{ background: '#f8fafc', borderRadius: '16px', width: '100%', maxWidth: '650px', position: 'relative', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)', overflow: 'hidden', display: 'flex', flexDirection: 'column', maxHeight: '90vh' }} onClick={e => e.stopPropagation()}>
            {/* Header */}
            <div style={{ background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', padding: '25px 30px', position: 'relative' }}>
              <button onClick={() => setSelectedBooking(null)} style={{ position: 'absolute', top: '20px', right: '20px', background: 'rgba(255,255,255,0.1)', border: 'none', width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: '#fff', transition: 'all 0.2s' }} onMouseOver={e => e.currentTarget.style.background = 'rgba(255,255,255,0.2)'} onMouseOut={e => e.currentTarget.style.background = 'rgba(255,255,255,0.1)'}>&times;</button>
              <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                <div style={{ background: 'rgba(255,255,255,0.15)', padding: '12px', borderRadius: '12px', color: '#38bdf8' }}>
                  <FaFileInvoiceDollar size={24} />
                </div>
                <div>
                  <h2 style={{ color: '#fff', margin: 0, fontSize: '1.5rem', fontWeight: '700' }}>Chi Tiết Đơn Hàng #{selectedBooking.id}</h2>
                  <p style={{ color: '#94a3b8', margin: '5px 0 0 0', fontSize: '0.9rem' }}>Đặt lúc: {new Date(selectedBooking.bookingDate).toLocaleString('vi-VN')}</p>
                </div>
              </div>
            </div>
            
            {/* Body */}
            <div style={{ padding: '30px', overflowY: 'auto' }}>
              
              {/* Tour Info Card */}
              <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
                <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                  <FaMap color="#3b82f6" /> Thông tin chuyến đi
                </h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Tên Tour</div>
                      <div style={{ color: '#0f172a', fontWeight: '600' }}>{selectedBooking.tourName || selectedBooking.tourTitle || 'Đang cập nhật'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaMapMarkerAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Địa điểm</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.destination || 'Đang cập nhật'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaCalendarAlt /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Ngày đi</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.travelDate ? new Date(selectedBooking.travelDate).toLocaleDateString('vi-VN') : 'N/A'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaUsers /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Số hành khách</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.numberOfPeople} người</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Customer Info Card */}
              <div style={{ background: '#fff', borderRadius: '12px', padding: '20px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03)', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
                <h3 style={{ margin: '0 0 15px 0', color: '#0f172a', fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>
                  <FaIdCard color="#8b5cf6" /> Thông tin khách hàng liên hệ
                </h3>
                
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaUser /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Họ và tên</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerName || `User #${selectedBooking.userId}`}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaPhone /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Số điện thoại</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerPhone || 'Không cung cấp'}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1' }}>
                    <div style={{ color: '#64748b', marginTop: '3px' }}><FaEnvelope /></div>
                    <div>
                      <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Email</div>
                      <div style={{ color: '#0f172a', fontWeight: '500' }}>{selectedBooking.customerEmail || 'Không cung cấp'}</div>
                    </div>
                  </div>
                  {selectedBooking.note && (
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px', gridColumn: '1 / -1', background: '#f8fafc', padding: '10px', borderRadius: '8px' }}>
                      <div style={{ color: '#64748b', marginTop: '3px' }}><FaStickyNote /></div>
                      <div>
                        <div style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '2px' }}>Ghi chú</div>
                        <div style={{ color: '#334155', fontStyle: 'italic', fontSize: '0.95rem' }}>"{selectedBooking.note}"</div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
              
              {/* Payment Summary Card */}
              <div style={{ background: 'linear-gradient(to right, #eff6ff, #dbeafe)', borderRadius: '12px', padding: '20px', border: '1px solid #bfdbfe' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                  <span style={{ color: '#1e3a8a', fontWeight: '600', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <FaCheckCircle /> Trạng thái đơn hàng:
                  </span>
                  <span style={{ 
                    fontWeight: 'bold', 
                    padding: '6px 12px', 
                    borderRadius: '20px', 
                    fontSize: '0.9rem',
                    background: selectedBooking.status === 'PENDING' ? '#fef3c7' : selectedBooking.status === 'CONFIRMED' ? '#dcfce7' : '#fee2e2',
                    color: selectedBooking.status === 'PENDING' ? '#92400e' : selectedBooking.status === 'CONFIRMED' ? '#166534' : '#991b1b',
                    border: `1px solid ${selectedBooking.status === 'PENDING' ? '#fde68a' : selectedBooking.status === 'CONFIRMED' ? '#bbf7d0' : '#fecaca'}`
                  }}>
                    {selectedBooking.status}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px dashed #93c5fd', paddingTop: '15px' }}>
                  <span style={{ color: '#1e3a8a', fontWeight: '700', fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <FaMoneyBillWave /> Tổng thanh toán:
                  </span>
                  <span style={{ color: '#be123c', fontWeight: '800', fontSize: '1.5rem', textShadow: '0 1px 1px rgba(0,0,0,0.05)' }}>
                    {formatPrice(selectedBooking.totalPrice)}
                  </span>
                </div>
              </div>

            </div>
            
            {/* Footer */}
            <div style={{ padding: '20px 30px', background: '#fff', borderTop: '1px solid #e2e8f0', display: 'flex', gap: '15px', justifyContent: 'flex-end', borderBottomLeftRadius: '16px', borderBottomRightRadius: '16px' }}>
              <button 
                onClick={() => setSelectedBooking(null)} 
                style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '10px 25px', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', boxShadow: '0 4px 6px -1px rgba(59, 130, 246, 0.4)', transition: 'all 0.2s' }}
                onMouseOver={e => e.currentTarget.style.transform = 'translateY(-2px)'}
                onMouseOut={e => e.currentTarget.style.transform = 'translateY(0)'}
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BookingManagement;
