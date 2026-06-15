import React from 'react';
import { Link, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { FaMapMarkedAlt, FaTicketAlt, FaSignOutAlt, FaTachometerAlt, FaHome, FaHotel, FaStar } from 'react-icons/fa';
import { AuthService } from '../../services/AuthService';
import '../../styles/admin.css';

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    AuthService.logout();
    navigate('/login');
  };

  return (
    <div className="admin-layout">
      {/* Sidebar */}
      <aside className="admin-sidebar">
        <div className="admin-logo" style={{ borderBottom: 'none', padding: '10px' }}>
        </div>
        <nav className="admin-nav">
          <Link 
            to="/" 
            className="admin-nav-item"
            style={{ marginBottom: '15px', color: '#e11d48', fontWeight: 'bold' }}
          >
            <FaHome /> Quay lại Trang Chủ
          </Link>
          <Link 
            to="/admin" 
            className={`admin-nav-item ${location.pathname === '/admin' ? 'active' : ''}`}
          >
            <FaTachometerAlt /> Dashboard
          </Link>
          <Link 
            to="/admin/tours" 
            className={`admin-nav-item ${location.pathname.includes('/admin/tours') ? 'active' : ''}`}
          >
            <FaMapMarkedAlt /> Quản lý Tour
          </Link>
          <Link 
            to="/admin/accommodations" 
            className={`admin-nav-item ${location.pathname.includes('/admin/accommodations') ? 'active' : ''}`}
          >
            <FaHotel /> Quản lý Nơi lưu trú
          </Link>
          <Link 
            to="/admin/utilities" 
            className={`admin-nav-item ${location.pathname.includes('/admin/utilities') ? 'active' : ''}`}
          >
            <FaStar /> Quản lý Tiện ích
          </Link>
          <Link 
            to="/admin/bookings" 
            className={`admin-nav-item ${location.pathname.includes('/admin/bookings') ? 'active' : ''}`}
          >
            <FaTicketAlt /> Quản lý Đơn Hàng
          </Link>
          <Link 
            to="/admin/vouchers" 
            className={`admin-nav-item ${location.pathname.includes('/admin/vouchers') ? 'active' : ''}`}
          >
            <FaTicketAlt /> Quản lý Voucher
          </Link>
        </nav>
        <div className="admin-sidebar-footer">
          <button onClick={handleLogout} className="btn-logout">
            <FaSignOutAlt /> Đăng xuất
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="admin-main">
        <header className="admin-topbar">
          <div className="topbar-right">
            <span>Xin chào, Admin</span>
          </div>
        </header>
        <div className="admin-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
