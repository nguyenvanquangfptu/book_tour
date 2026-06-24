param()
$body = @{
    email = 'test500@example.com'
    password = 'password123'
    fullName = 'Test User'
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri 'https://book-tour-fqiz.onrender.com/api/auth/register' -Method Post -Body $body -ContentType 'application/json'
    Write-Host $response.data.token
} catch {
    Write-Host "Error:"
    Write-Host $_.Exception.Response.Content.ReadAsStringAsync().Result
}
