import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Footer from './Footer';
import { useAuthStore } from '../store/useAuthStore';
import { useCartStore } from '../store/useCartStore';
import { useWishlistStore } from '../store/useWishlistStore';

const PublicLayout: React.FC = () => {
  const isAuthenticated = useAuthStore(state => state.isAuthenticated);
  const fetchCart = useCartStore(state => state.fetchCart);
  const fetchWishlist = useWishlistStore(state => state.fetchWishlist);
  const clearWishlist = useWishlistStore(state => state.clearWishlist);

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
      fetchWishlist();
    } else {
      // Wishlist gio gan voi tai khoan: dang xuat thi xoa state tren may,
      // du lieu tren server van con nguyen cho lan dang nhap sau.
      clearWishlist();
    }
  }, [isAuthenticated, fetchCart, fetchWishlist, clearWishlist]);

  return (
    <>
      <Navbar />
      <Outlet />
      <Footer />
    </>
  );
};

export default PublicLayout;
