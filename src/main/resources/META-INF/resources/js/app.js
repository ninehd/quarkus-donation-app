async function loadStats() {
    try {
        const response = await fetch('/api/donations/stats');
        const data = await response.json();
        const donorCountEl = document.getElementById('donorCount');
        const totalAmountEl = document.getElementById('totalAmount');

        if (donorCountEl) donorCountEl.textContent = data.donationCount;
        if (totalAmountEl) totalAmountEl.textContent = data.totalAmount.toFixed(2);
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// Load stats on page load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadStats);
} else {
    loadStats();
}

// Form submission
const donationForm = document.getElementById('donationForm');
if (donationForm) {
    donationForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = {
            donorName: document.getElementById('donorName').value,
            donorEmail: document.getElementById('donorEmail').value,
            amount: parseFloat(document.getElementById('amount').value),
            message: document.getElementById('message').value || null
        };

        try {
            const loadingEl = document.getElementById('loading');
            loadingEl.style.display = 'block';

            const response = await fetch('/api/donations/paypal/initiate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Error initiating donation');
            }

            // Get the approval URL from JSON response
            const data = await response.json();

            // Redirect to PayPal
            window.location.href = data.approvalUrl;

        } catch (error) {
            document.getElementById('error').textContent = error.message;
            document.getElementById('loading').style.display = 'none';
        }
    });
}