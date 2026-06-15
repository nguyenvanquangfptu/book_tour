import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import ToursPage from './pages/ToursPage';
import AboutPage from './pages/AboutPage';
import ContactPage from './pages/ContactPage';
import TourDetail from './pages/TourDetail';
import Checkout from './pages/Checkout';
import Login from './pages/Login';
import Register from './pages/Register';
import CartPage from './pages/CartPage';
import ProfilePage from './pages/ProfilePage';
import PaymentResult from './pages/PaymentResult';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentCancel from './pages/PaymentCancel';
import FAQPage from './pages/FAQPage';
import PrivacyPolicyPage from './pages/PrivacyPolicyPage';
import TermsOfServicePage from './pages/TermsOfServicePage';
import RefundPolicyPage from './pages/RefundPolicyPage';
import { CartProvider } from './context/CartContext';
import ScrollToTop from './components/ScrollToTop';

// Admin Components
import AdminLayout from './components/admin/AdminLayout';
import Dashboard from './pages/admin/Dashboard';
import TourManagement from './pages/admin/TourManagement';
import BookingManagement from './pages/admin/BookingManagement';
import VoucherManagement from './pages/admin/VoucherManagement';
import AccommodationManagement from './pages/admin/AccommodationManagement';
import UtilityManagement from './pages/admin/UtilityManagement';

const App: React.FC = () => {
  return (
    <Router>
      <ScrollToTop />
      <CartProvider>
        <div className="app">
          <Routes>
              {/* Public Routes with Navbar and Footer */}
              <Route path="/" element={<><Navbar /><Home /><Footer /></>} />
              <Route path="/tours" element={<><Navbar /><ToursPage /><Footer /></>} />
              <Route path="/about" element={<><Navbar /><AboutPage /><Footer /></>} />
              <Route path="/contact" element={<><Navbar /><ContactPage /><Footer /></>} />
              <Route path="/tours/:id" element={<><Navbar /><TourDetail /><Footer /></>} />
              <Route path="/checkout/:id" element={<><Navbar /><Checkout /><Footer /></>} />
              <Route path="/cart" element={<><Navbar /><CartPage /><Footer /></>} />
              <Route path="/profile" element={<><Navbar /><ProfilePage /><Footer /></>} />
              <Route path="/payment-result" element={<><Navbar /><PaymentResult /><Footer /></>} />
              <Route path="/payment/success" element={<><Navbar /><PaymentSuccess /><Footer /></>} />
              <Route path="/payment/cancel" element={<><Navbar /><PaymentCancel /><Footer /></>} />
              <Route path="/login" element={<><Navbar /><Login /><Footer /></>} />
              <Route path="/register" element={<><Navbar /><Register /><Footer /></>} />
              <Route path="/faq" element={<><Navbar /><FAQPage /><Footer /></>} />
              <Route path="/privacy-policy" element={<><Navbar /><PrivacyPolicyPage /><Footer /></>} />
              <Route path="/terms-of-service" element={<><Navbar /><TermsOfServicePage /><Footer /></>} />
              <Route path="/refund-policy" element={<><Navbar /><RefundPolicyPage /><Footer /></>} />

              {/* Admin Routes */}
              <Route path="/admin" element={<AdminLayout />}>
                <Route index element={<Dashboard />} />
                <Route path="tours" element={<TourManagement />} />
                <Route path="bookings" element={<BookingManagement />} />
                <Route path="vouchers" element={<VoucherManagement />} />
                <Route path="accommodations" element={<AccommodationManagement />} />
                <Route path="utilities" element={<UtilityManagement />} />
              </Route>

              {/* Fallback route */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </CartProvider>
    </Router>
  );
}

export default App;
