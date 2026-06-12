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
import { CartProvider } from './context/CartContext';

// Admin Components
import AdminLayout from './components/admin/AdminLayout';
import Dashboard from './pages/admin/Dashboard';
import TourManagement from './pages/admin/TourManagement';
import VoucherManagement from './pages/admin/VoucherManagement';
import BookingManagement from './pages/admin/BookingManagement';

function App() {
  return (
    <Router>
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
            <Route path="/login" element={<><Navbar /><Login /><Footer /></>} />
            <Route path="/register" element={<><Navbar /><Register /><Footer /></>} />

            {/* Admin Routes without global Navbar and Footer */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<Dashboard />} />
              <Route path="tours" element={<TourManagement />} />
              <Route path="bookings" element={<BookingManagement />} />
              <Route path="vouchers" element={<VoucherManagement />} />
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
