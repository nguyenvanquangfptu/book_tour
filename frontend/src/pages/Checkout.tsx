import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { FaChevronLeft, FaShieldAlt, FaCheckCircle } from 'react-icons/fa';
import Swal from 'sweetalert2';
import { formatPrice } from '../utils/formatPrice';
import { BookingService } from '../services/BookingService';
import { VoucherService } from '../services/VoucherService';
import { useTranslation } from 'react-i18next';
import '../styles/checkout.css';

interface LocationState {
  tourId: number;
  tourTitle: string;
  guests: number;
  startDate: string;
  totalPrice: number;
}

const Checkout: React.FC = () => {
  const { t } = useTranslation();
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
  
  // Voucher states
  const [voucherCode, setVoucherCode] = useState('');
  const [discountAmount, setDiscountAmount] = useState(0);
  const [voucherMessage, setVoucherMessage] = useState('');
  const [appliedVoucherId, setAppliedVoucherId] = useState<number | null>(null);

  // Nếu truy cập trực tiếp trang checkout mà không có data từ Tour Detail
  if (!state) {
    return (
      <div className="container checkout-empty">
        <h2>{t('checkout.noTourInfoTitle')}</h2>
        <p>{t('checkout.noTourInfoDesc')}</p>
        <Link to="/tours" className="btn btn-primary">{t('checkout.viewTours')}</Link>
      </div>
    );
  }

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleApplyVoucher = async () => {
    if (!voucherCode) return;
    try {
      setVoucherMessage('');
      const voucher = await VoucherService.getVoucherByCode(voucherCode);
      if (!voucher || !voucher.isActive) {
        setVoucherMessage(t('checkout.voucherInvalid'));
        setDiscountAmount(0);
        setAppliedVoucherId(null);
        return;
      }
      
      const now = new Date();
      if (voucher.validFrom && now < new Date(voucher.validFrom)) {
        setVoucherMessage(t('checkout.voucherNotStarted'));
        setDiscountAmount(0);
        setAppliedVoucherId(null);
        return;
      }
      if (voucher.validUntil && now > new Date(voucher.validUntil)) {
        setVoucherMessage(t('checkout.voucherExpired'));
        setDiscountAmount(0);
        setAppliedVoucherId(null);
        return;
      }
      if (voucher.usageLimit && voucher.usedCount >= voucher.usageLimit) {
        setVoucherMessage(t('checkout.voucherLimitReached'));
        setDiscountAmount(0);
        setAppliedVoucherId(null);
        return;
      }
      if (state.totalPrice < (voucher.minOrderValue || 0)) {
        setVoucherMessage(t('checkout.voucherMinOrder', { min: formatPrice(voucher.minOrderValue) }));
        setDiscountAmount(0);
        setAppliedVoucherId(null);
        return;
      }
      
      let discount = 0;
      if (voucher.discountAmount) {
        discount = voucher.discountAmount;
      } else if (voucher.discountPercentage) {
        discount = state.totalPrice * (voucher.discountPercentage / 100);
        if (voucher.maxDiscount && discount > voucher.maxDiscount) {
          discount = voucher.maxDiscount;
        }
      }
      
      setDiscountAmount(discount);
      setAppliedVoucherId(voucher.id);
      setVoucherMessage(t('checkout.voucherSuccess'));
    } catch (error) {
      setVoucherMessage(t('checkout.voucherNotFound'));
      setDiscountAmount(0);
      setAppliedVoucherId(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // 1. Gửi request tạo Booking
      const bookingRequest: any = {
        tourId: state.tourId,
        bookingDate: new Date().toISOString(),
        travelDate: state.startDate,
        numberOfPeople: state.guests,
        totalPrice: Math.max(0, state.totalPrice - discountAmount),
        customerName: formData.fullName,
        customerEmail: formData.email,
        customerPhone: formData.phone,
        note: formData.note
      };
      
      if (appliedVoucherId) {
        bookingRequest.voucherId = appliedVoucherId;
      }

      const bookingResponse = await BookingService.createBooking(bookingRequest);
      
      const bookingData = bookingResponse?.data || bookingResponse;
      
      if (bookingData && bookingData.id) {
        Swal.fire({
          icon: 'success',
          title: t('checkout.bookingSuccessTitle'),
          text: t('checkout.bookingSuccessText'),
          confirmButtonColor: '#3b82f6'
        }).then(() => {
          navigate('/profile');
        });
      }
    } catch (error) {
      console.error('Checkout failed', error);
      Swal.fire({
        icon: 'error',
        title: t('checkout.bookingErrorTitle'),
        text: t('checkout.bookingErrorText'),
        confirmButtonColor: '#3b82f6'
      });
    } finally {
      setLoading(false);
    }
  };

  const isEmailInvalid = formData.email.length > 0 && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.(com|vn|net|org|edu|gov|info|io)$/.test(formData.email);
  const isPhoneInvalid = formData.phone.length > 0 && !/^(0|\+84)[0-9]{9}$/.test(formData.phone);
  const isFormInvalid = isEmailInvalid || isPhoneInvalid;

  return (
    <div className="checkout-page container">
      <div className="checkout-header">
        <button onClick={() => navigate(-1)} className="btn-back">
          <FaChevronLeft /> {t('checkout.back')}
        </button>
        <h1>{t('checkout.secureCheckout')}</h1>
      </div>

      <div className="checkout-grid">
        {/* Form Thông Tin */}
        <div className="checkout-form-section glass-card">
          <h2 className="section-title">{t('checkout.contactInfo')}</h2>
          <form onSubmit={handleSubmit} id="checkout-form" autoComplete="off">
            <div className="input-group">
              <label className="input-label">{t('checkout.fullName')}</label>
              <input 
                type="text" 
                name="fullName"
                className="input-field" 
                placeholder={t('checkout.fullNamePlaceholder')}
                value={formData.fullName}
                onChange={handleInputChange}
                autoComplete="off"
                required 
              />
            </div>
            
            <div className="form-row">
              <div className="input-group">
                <label className="input-label">{t('checkout.email')}</label>
                <input 
                  type="email" 
                  name="email"
                  className="input-field" 
                  placeholder={t('checkout.emailPlaceholder')}
                  value={formData.email}
                  onChange={handleInputChange}
                  autoComplete="off"
                  required 
                />
                {isEmailInvalid && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>{t('checkout.emailInvalid')}</span>}
              </div>
              
              <div className="input-group">
                <label className="input-label">{t('checkout.phone')}</label>
                <input 
                  type="tel" 
                  name="phone"
                  className="input-field" 
                  placeholder={t('checkout.phonePlaceholder')}
                  value={formData.phone}
                  onChange={handleInputChange}
                  autoComplete="off"
                  required 
                />
                {isPhoneInvalid && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', fontWeight: '500'}}>{t('checkout.phoneInvalid')}</span>}
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">{t('checkout.note')}</label>
              <textarea 
                name="note"
                className="input-field" 
                rows={4}
                placeholder={t('checkout.notePlaceholder')}
                value={formData.note}
                onChange={handleInputChange}
              ></textarea>
            </div>
          </form>

          <div className="security-notice">
            <FaShieldAlt className="shield-icon" />
            <p>{t('checkout.securityNotice')}</p>
          </div>
        </div>

        {/* Tóm tắt Booking */}
        <div className="checkout-summary glass-card">
          <h2 className="section-title">{t('checkout.tripSummary')}</h2>
          
          <div className="summary-tour-info">
            <h3>{state.tourTitle}</h3>
            <div className="summary-meta">
              <p><strong>{t('checkout.departureDate')}</strong> {state.startDate}</p>
              <p><strong>{t('checkout.guests')}</strong> {state.guests} {t('checkout.guestsCount')}</p>
            </div>
          </div>

          <div className="summary-divider"></div>

          <div className="voucher-section">
            <div className="voucher-input-wrapper">
              <input 
                type="text" 
                className="voucher-input" 
                placeholder={t('checkout.voucherPlaceholder')}
                value={voucherCode}
                onChange={(e) => setVoucherCode(e.target.value)}
              />
              <button type="button" className="voucher-btn" onClick={handleApplyVoucher}>
                {t('checkout.apply')}
              </button>
            </div>
            {voucherMessage && (
              <p style={{ marginTop: '12px', fontSize: '0.9rem', fontWeight: 500, color: discountAmount > 0 ? '#16a34a' : '#dc2626' }}>
                {voucherMessage}
              </p>
            )}
          </div>

          <div className="summary-divider"></div>

          <div className="price-details">
            <div className="price-row">
              <span>{t('checkout.ticketPrice', { guests: state.guests })}</span>
              <span>{formatPrice(state.totalPrice)}</span>
            </div>
            <div className="price-row">
              <span>{t('checkout.taxAndFee')}</span>
              <span>0 VNĐ</span>
            </div>
            {discountAmount > 0 && (
              <div className="price-row" style={{ color: 'var(--success)' }}>
                <span>{t('checkout.discount')}</span>
                <span>-{formatPrice(discountAmount)}</span>
              </div>
            )}
            <div className="price-row total">
              <span>{t('checkout.totalPayment')}</span>
              <span className="total-amount">{formatPrice(Math.max(0, state.totalPrice - discountAmount))}</span>
            </div>
          </div>

          <button 
            type="submit" 
            form="checkout-form" 
            className="btn btn-primary"
            style={{ backgroundColor: '#3b82f6', borderColor: '#3b82f6', width: '100%', padding: '15px', borderRadius: '8px', fontSize: '1.1rem', fontWeight: 'bold', display: 'flex', justifyContent: 'center', alignItems: 'center' }}
            disabled={loading || isFormInvalid}
          >
            {loading ? t('checkout.processing') : (
              <>
                <FaCheckCircle style={{ marginRight: '8px' }} /> {t('checkout.confirmBooking')}
              </>
            )}
          </button>
          <p className="vnpay-notice" style={{ color: '#64748b', textAlign: 'center', marginTop: '12px' }}>
            {t('checkout.adminNotice')}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
