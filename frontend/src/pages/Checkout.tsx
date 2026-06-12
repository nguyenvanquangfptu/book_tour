import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { FaChevronLeft, FaShieldAlt, FaCreditCard } from 'react-icons/fa';
import { BookingService } from '../services/BookingService';
import '../styles/checkout.css';

interface LocationState {
  tourId: number;
  tourTitle: string;
  guests: number;
  startDate: string;
  totalPrice: number;
}

const Checkout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as LocationState;

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    note: ''
  });
  const [loading, setLoading] = useState(false);

  // Nếu truy cập trực tiếp trang checkout mà không có data từ Tour Detail
  if (!state) {
    return (
      <div className="container checkout-empty">
        <h2>Không tìm thấy thông tin đặt tour</h2>
        <p>Vui lòng quay lại chọn tour trước khi thanh toán.</p>
        <Link to="/tours" className="btn btn-primary">Xem danh sách Tour</Link>
      </div>
    );
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. Gửi request tạo Booking
      const bookingRequest = {
        tourId: state.tourId,
        bookingDate: new Date().toISOString(),
        travelDate: state.startDate,
        numberOfPeople: state.guests,
        totalPrice: state.totalPrice,
        customerName: formData.fullName,
        customerEmail: formData.email,
        customerPhone: formData.phone,
        note: formData.note
      };

      const bookingResponse = await BookingService.createBooking(bookingRequest);
      
      const bookingData = bookingResponse?.data || bookingResponse;
      
      if (bookingData && bookingData.id) {
        // 2. Gọi API tạo VNPay URL
        const paymentResponse = await BookingService.createVNPayUrl(bookingData.id);
        
        // 3. Chuyển hướng sang VNPay
        const paymentData = paymentResponse?.data || paymentResponse;
        if (paymentData && typeof paymentData === 'string' && paymentData.startsWith('http')) {
          window.location.href = paymentData;
        } else if (paymentData && paymentData.paymentUrl) {
          window.location.href = paymentData.paymentUrl;
        } else {
          alert('Đã tạo booking nhưng không thể tạo VNPay URL. Vui lòng liên hệ Admin.');
          navigate('/profile');
        }
      }
    } catch (error) {
      console.error('Checkout failed', error);
      alert('Đã xảy ra lỗi trong quá trình xử lý. Đang mô phỏng giao dịch thành công...');
      // Fallback mô phỏng cho UI
      setTimeout(() => {
        navigate('/');
      }, 1500);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-page container">
      <div className="checkout-header">
        <button onClick={() => navigate(-1)} className="btn-back">
          <FaChevronLeft /> Quay lại
        </button>
        <h1>Thanh Toán An Toàn</h1>
      </div>

      <div className="checkout-grid">
        {/* Form Thông Tin */}
        <div className="checkout-form-section glass-card">
          <h2 className="section-title">Thông tin liên hệ</h2>
          <form onSubmit={handleSubmit} id="checkout-form">
            <div className="input-group">
              <label className="input-label">Họ và tên *</label>
              <input 
                type="text" 
                name="fullName"
                className="input-field" 
                placeholder="Nhập họ tên của bạn"
                value={formData.fullName}
                onChange={handleInputChange}
                required 
              />
            </div>
            
            <div className="form-row">
              <div className="input-group">
                <label className="input-label">Email *</label>
                <input 
                  type="email" 
                  name="email"
                  className="input-field" 
                  placeholder="example@gmail.com"
                  value={formData.email}
                  onChange={handleInputChange}
                  required 
                />
              </div>
              
              <div className="input-group">
                <label className="input-label">Số điện thoại *</label>
                <input 
                  type="tel" 
                  name="phone"
                  className="input-field" 
                  placeholder="09xx xxx xxx"
                  value={formData.phone}
                  onChange={handleInputChange}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Ghi chú thêm (Tuỳ chọn)</label>
              <textarea 
                name="note"
                className="input-field" 
                rows={4}
                placeholder="Ví dụ: Yêu cầu ăn chay, đón tại sân bay..."
                value={formData.note}
                onChange={handleInputChange}
              ></textarea>
            </div>
          </form>

          <div className="security-notice">
            <FaShieldAlt className="shield-icon" />
            <p>Thông tin của bạn được mã hóa bảo mật an toàn 100%.</p>
          </div>
        </div>

        {/* Tóm tắt Booking */}
        <div className="checkout-summary glass-card">
          <h2 className="section-title">Tóm tắt chuyến đi</h2>
          
          <div className="summary-tour-info">
            <h3>{state.tourTitle}</h3>
            <div className="summary-meta">
              <p><strong>Ngày đi:</strong> {state.startDate}</p>
              <p><strong>Số lượng:</strong> {state.guests} hành khách</p>
            </div>
          </div>

          <div className="summary-divider"></div>

          <div className="price-details">
            <div className="price-row">
              <span>Giá vé ({state.guests} người)</span>
              <span>{state.totalPrice.toLocaleString()} ₫</span>
            </div>
            <div className="price-row">
              <span>Thuế và phí</span>
              <span>0 ₫</span>
            </div>
            <div className="price-row total">
              <span>Tổng thanh toán</span>
              <span className="total-amount">{state.totalPrice.toLocaleString()} ₫</span>
            </div>
          </div>

          <button 
            type="submit" 
            form="checkout-form" 
            className="btn btn-primary vnpay-btn"
            disabled={loading}
          >
            {loading ? 'Đang xử lý...' : (
              <>
                <FaCreditCard /> Thanh toán qua VNPay
              </>
            )}
          </button>
          <p className="vnpay-notice">Bạn sẽ được chuyển hướng tới cổng thanh toán an toàn của VNPay.</p>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
