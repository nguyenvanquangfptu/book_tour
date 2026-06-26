import React from 'react';
import { useTranslation } from 'react-i18next';
import { useWishlistStore } from '../../store/useWishlistStore';
import { Link } from 'react-router-dom';
import { FaTrash, FaMapMarkerAlt, FaClock } from 'react-icons/fa';

const WishlistTab: React.FC = () => {
  const { t } = useTranslation();
  const { wishlist, removeFromWishlist } = useWishlistStore();

  return (
    <div>
      <h2 style={{ marginBottom: '20px', color: '#0f172a' }}>{t('navbar.wishlist')}</h2>
      
      {wishlist.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px 0', color: '#64748b' }}>
          <p>Danh sách yêu thích của bạn đang trống.</p>
          <Link to="/tours" className="btn btn-primary" style={{ marginTop: '15px', display: 'inline-block' }}>
            Khám phá Tours
          </Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
          {wishlist.map((item) => (
            <div key={item.id} style={{ border: '1px solid #e2e8f0', borderRadius: '12px', overflow: 'hidden', position: 'relative', display: 'flex', flexDirection: 'column' }}>
              <img src={item.imageUrl} alt={item.title} style={{ width: '100%', height: '180px', objectFit: 'cover' }} />
              
              <button 
                onClick={() => removeFromWishlist(item.id)}
                style={{ position: 'absolute', top: '10px', right: '10px', background: '#fff', border: 'none', width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', boxShadow: '0 2px 4px rgba(0,0,0,0.1)', color: '#ef4444' }}
                title="Bỏ yêu thích"
              >
                <FaTrash />
              </button>

              <div style={{ padding: '15px', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <h3 style={{ fontSize: '1.1rem', margin: '0 0 10px 0', color: '#0f172a', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{item.title}</h3>
                
                <div style={{ display: 'flex', gap: '15px', fontSize: '0.85rem', color: '#64748b', marginBottom: '15px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                    <FaMapMarkerAlt /> {item.destination}
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                    <FaClock /> {item.duration}
                  </div>
                </div>

                <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ color: '#f97316', fontWeight: 'bold', fontSize: '1.1rem' }}>
                    {item.price.toLocaleString('vi-VN')} VNĐ
                  </div>
                  <Link to={`/tours/${item.id}`} style={{ padding: '8px 15px', background: '#3b82f6', color: '#fff', borderRadius: '6px', textDecoration: 'none', fontSize: '0.9rem', fontWeight: '500' }}>
                    Chi tiết
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default WishlistTab;
