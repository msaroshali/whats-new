function search() {
  const keyword = document.getElementById("keyword").value;
  const username = document.getElementById("username").value;

  fetch(`http://localhost:7070/search?keyword=${encodeURIComponent(keyword)}&username=${encodeURIComponent(username)}`)
  .then(response => response.json())
  .then(data => {
      const results = document.getElementById("results");
      results.innerHTML = ""; // Clear previous results

      if (!Array.isArray(data)) {
          results.innerHTML = `<p style="color:red;">Unexpected response: ${JSON.stringify(data)}</p>`;
          return;
      }

      if (data.length === 0) {
          results.innerHTML = "<p>No tweets found.</p>";
          return;
      }

      // 1. Create the table structure outside the loop
      let tableHTML = `
          <table id="example" class="display">
              <thead>
                  <tr>
                      <th>Date and Time</th>
                      <th>Tweet</th>
                  </tr>
              </thead>
              <tbody>
      `;

      // 2. Loop through the data to create a table row for each tweet
      data.forEach(tweet => {
          if (tweet.error) {
              // If there's an error, display it separately
              results.innerHTML += `<p style="color:red;">Error: ${tweet.error}</p>`;
          } else {
              tableHTML += `
                  <tr>
                      <td>${tweet.date}</td>
                      <td>${tweet.content}</td>
                  </tr>
              `;
          }
      });

      // 3. Close the table structure and add it to the DOM
      tableHTML += `
              </tbody>
          </table>
      `;

      results.innerHTML += tableHTML;
      new DataTable('#example', {
        layout: {
            bottomEnd: {
                paging: {
                    firstLast: false
                }
            }
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

    // Event listener for clicking on a thumbnail
    gallery.addEventListener('click', (event) => {
        const thumbnail = event.target.closest('.thumbnail');
        if (thumbnail) {
            const thumbnailId = thumbnail.getAttribute('data-id');

            // Find the currently selected thumbnail, if any
            const currentlySelected = gallery.querySelector('.thumbnail.selected');

            // If a different thumbnail is already selected, deselect it
            if (currentlySelected && currentlySelected !== thumbnail) {
                currentlySelected.classList.remove('selected');
            }

            // Toggle the 'selected' class on the clicked thumbnail
            thumbnail.classList.toggle('selected');

            // If the thumbnail is now selected, update the username field
            if (thumbnail.classList.contains('selected')) {
                usernameField.value = thumbnailId;
            } else {
                // If the thumbnail is deselected, clear the username field
                usernameField.value = '';
            }
        }
    });
});

