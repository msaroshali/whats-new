function search() {
  const keyword = document.getElementById("keyword").value;
  const username = document.getElementById("username").value;

  fetch(`http://localhost:7070/search?keyword=${encodeURIComponent(keyword)}&username=${encodeURIComponent(username)}`)
      .then(response => response.json())
      .then(data => {
          const results = document.getElementById("results");
          results.innerHTML = "";

          if (!Array.isArray(data)) {
              results.innerHTML = `<p style="color:red;">Unexpected response: ${JSON.stringify(data)}</p>`;
              return;
          }

          if (data.length === 0) {
              results.innerHTML = "<p>No tweets found.</p>";
              return;
          }

          data.forEach(tweet => {
              if (tweet.error) {
                  results.innerHTML += `<p style="color:red;">Error: ${tweet.error}</p>`;
              } else {
                  results.innerHTML += `<p><strong>${tweet.date}</strong>: ${tweet.content}</p>`;
              }
          });
      })
      .catch(err => {
          document.getElementById("results").innerHTML = `<p style="color:red;">Fetch error: ${err}</p>`;
      });
}
document.addEventListener('DOMContentLoaded', () => {
    const gallery = document.querySelector('.gallery');
    const usernameField = document.getElementById('username');
    let selectedIds = [];

    // Event listener for clicking on a thumbnail
    gallery.addEventListener('click', (event) => {
        const thumbnail = event.target.closest('.thumbnail');
        if (thumbnail) {
            const thumbnailId = thumbnail.getAttribute('data-id');

            // Fill the username field with the clicked thumbnail's id
            usernameField.value = thumbnailId;

            // Toggle the 'selected' class
            thumbnail.classList.toggle('selected');

            // Add or remove the ID from the selectedIds array
            if (thumbnail.classList.contains('selected')) {
                if (!selectedIds.includes(thumbnailId)) {
                    selectedIds.push(thumbnailId);
                }
            } else {
                selectedIds = selectedIds.filter(id => id !== thumbnailId);
            }

            console.log('Selected IDs:', selectedIds);
        }
    });
});