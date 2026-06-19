import React from 'react';
import { FaUser, FaHistory, FaLock, FaFileInvoiceDollar, FaSignOutAlt } from 'react-icons/fa';
import { useAuthStore } from '../../store/useAuthStore';
import { useNavigate } from 'react-router-dom';

interface ProfileSidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  clearMessage: () => void;
}

const ProfileSidebar: React.FC<ProfileSidebarProps> = ({ activeTab, setActiveTab, clearMessage }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    useAuthStore.getState().logout();
    navigate('/login');
  };

  const tabs = [
    { id: 'profile', label: 'Thông tin hồ sơ', icon: <FaUser /> },
    { id: 'bookings', label: 'Lịch sử đặt tour', icon: <FaHistory /> },
    { id: 'password', label: 'Đổi mật khẩu', icon: <FaLock /> },
    { id: 'billing', label: 'Hóa đơn của tôi', icon: <FaFileInvoiceDollar /> },
  ];

  return (
    <div style={{ width: '250px', flexShrink: 0, background: '#fff', padding: '20px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)', height: 'fit-content' }}>
      <h2 style={{ fontSize: '1.2rem', marginBottom: '20px', color: '#0f172a' }}>Tài Khoản</h2>
      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {tabs.map(tab => (
          <li 
            key={tab.id}
            onClick={() => { setActiveTab(tab.id); clearMessage(); }}
            style={{ 
              padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', marginBottom: '5px', 
              background: activeTab === tab.id ? '#f1f5f9' : 'transparent', 
              color: activeTab === tab.id ? '#2563eb' : '#475569', 
              fontWeight: activeTab === tab.id ? '600' : '400', 
              display: 'flex', alignItems: 'center', gap: '10px' 
            }}
          >
            {tab.icon} {tab.label}
          </li>
        ))}
        <li 
          onClick={handleLogout}
          style={{ padding: '12px 15px', cursor: 'pointer', borderRadius: '8px', background: 'transparent', color: '#e11d48', fontWeight: '400', display: 'flex', alignItems: 'center', gap: '10px', marginTop: '10px', borderTop: '1px solid #e2e8f0' }}
        >
          <FaSignOutAlt /> Đăng xuất
        </li>
      </ul>
    </div>
  );
};

export default ProfileSidebar;
