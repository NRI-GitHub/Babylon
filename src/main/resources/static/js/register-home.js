document.addEventListener("DOMContentLoaded", function() {
    onPageLoaded();
});


function onPageLoaded(){
    document.getElementById('registerForm').addEventListener('submit', function(e) {
        const roomValue = document.getElementById('roomName').value;
        const expectedValue = 'nri5764a7cc';  // replace with your expected value

        if (roomValue !== expectedValue) {
            e.preventDefault();  // prevent form submission
            alert('Invalid room value. Please enter the correct value.');
        }
    });
}