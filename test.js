const http = require('https');
const data = JSON.stringify({ email: 'test5000@example.com', password: 'password123', fullName: 'Test User' });
const req = http.request('https://book-tour-fqiz.onrender.com/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', 'Content-Length': data.length }
}, (res) => {
  let body = '';
  res.on('data', chunk => body += chunk);
  res.on('end', () => console.log(res.statusCode, body));
});
req.on('error', console.error);
req.write(data);
req.end();
