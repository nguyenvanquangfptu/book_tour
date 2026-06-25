const http = require('https');
const data = JSON.stringify({ username: 'testuser5000', password: 'password123' });
const req = http.request('https://book-tour-fqiz.onrender.com/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', 'Content-Length': data.length }
}, (res) => {
  let body = '';
  res.on('data', chunk => body += chunk);
  res.on('end', () => {
    const response = JSON.parse(body);
    console.log("Token:", response.data ? response.data.token : body);
    
    if (response.data && response.data.token) {
        const token = response.data.token;
        const bookingData = JSON.stringify({
            tourId: 1,
            travelDate: "2026-06-25",
            numberOfPeople: 2,
            totalPrice: 10000,
            customerName: "Test",
            customerEmail: "test@example.com",
            customerPhone: "0123456789"
        });
        const req2 = http.request('https://book-tour-fqiz.onrender.com/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': bookingData.length,
                'Authorization': 'Bearer ' + token
            }
        }, (res2) => {
            let body2 = '';
            res2.on('data', chunk => body2 += chunk);
            res2.on('end', () => console.log("Booking:", res2.statusCode, body2));
        });
        req2.write(bookingData);
        req2.end();
    }
  });
});
req.on('error', console.error);
req.write(data);
req.end();
