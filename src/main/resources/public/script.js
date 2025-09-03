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
        ordering: false

    });
  })
  .catch(err => {
      document.getElementById("results").innerHTML = `<p style="color:red;">Fetch error: ${err}</p>`;
  });
}

//Gallery of news sources
document.addEventListener('DOMContentLoaded', () => {
    const gallery = document.querySelector('.gallery');
    const usernameField = document.getElementById('username');
  
    if (!gallery) return; // safety
  
    gallery.addEventListener('click', (event) => {
      const thumbnail = event.target.closest('.thumbnail');
      if (!thumbnail) return;
  
      const thumbnailId = thumbnail.getAttribute('data-id');
  
      // find previously selected thumbnail (single-select behavior)
      const prev = gallery.querySelector('.thumbnail[data-selected="true"]');
  
      // If clicking the same selected thumbnail -> deselect
      if (prev && prev === thumbnail) {
        prev.removeAttribute('data-selected');
        // remove selected utility classes
        prev.classList.remove('ring-4', 'ring-blue-500', 'scale-95');
        // hide its check icon
        const prevCheck = prev.querySelector('.check-icon');
        if (prevCheck) prevCheck.classList.add('hidden');
  
        // clear username field
        if (usernameField) usernameField.value = '';
        return;
      }
  
      // Deselect previous if different
      if (prev && prev !== thumbnail) {
        prev.removeAttribute('data-selected');
        prev.classList.remove('ring-4', 'ring-blue-500', 'scale-95');
        const prevCheck = prev.querySelector('.check-icon');
        if (prevCheck) prevCheck.classList.add('hidden');
      }
  
      // Select this thumbnail
      thumbnail.setAttribute('data-selected', 'true');
      thumbnail.classList.add('ring-4', 'ring-blue-500', 'scale-95');
  
      // show check icon
      const check = thumbnail.querySelector('.check-icon');
      if (check) check.classList.remove('hidden');
  
      // write to username field
      if (usernameField) usernameField.value = thumbnailId;
    });
  });

async function fetchLatest(limit = 50) {
    const res = await fetch(`/latest?limit=${limit}`);
    const tweets = await res.json();
  
    const resultsDiv = document.getElementById("results");
    resultsDiv.innerHTML = ""; // clear old
    
    // 1. Create the table structure outside the loop
       let tableHTML = `
       <table id="example" class="display">
           <thead>
               <tr>
                   <th>Source</th>
                   <th>Date and Time</th>
                   <th>Tweet</th>
               </tr>
           </thead>
           <tbody>
   `;

    tweets.forEach(t => {
        tableHTML += `
        <tr>
            <td>${t.username}</td>
            <td>${t.date}</td>
            <td>${t.content}</td>
        </tr>
    `;
         // div.innerHTML = `<strong>@${t.username}</strong>: ${t.content} <em>(${t.date})</em>`;
      //resultsDiv.appendChild(div);
    });
          tableHTML += `
              </tbody>
          </table>
      `;

      results.innerHTML += tableHTML;
      new DataTable('#example', {
        ordering: false

    });

  }


  // Dark mode toggle
  // Theme toggle with persistence
// Dark mode toggle with persistence for Tailwind
document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("theme-toggle");
    const icon = toggleBtn.querySelector("i");
  
    // On page load → set theme from localStorage or system preference
    if (
      localStorage.theme === "dark" ||
      (!("theme" in localStorage) && window.matchMedia("(prefers-color-scheme: dark)").matches)
    ) {
      document.documentElement.classList.add("dark");
      icon.classList.remove("fa-moon");
      icon.classList.add("fa-sun");
    } else {
      document.documentElement.classList.remove("dark");
      icon.classList.remove("fa-sun");
      icon.classList.add("fa-moon");
    }
  
    // Toggle on button click
    toggleBtn.addEventListener("click", () => {
      const isDark = document.documentElement.classList.toggle("dark");
      if (isDark) {
        localStorage.theme = "dark";
        icon.classList.remove("fa-moon");
        icon.classList.add("fa-sun");
      } else {
        localStorage.theme = "light";
        icon.classList.remove("fa-sun");
        icon.classList.add("fa-moon");
      }
    });
  });
  