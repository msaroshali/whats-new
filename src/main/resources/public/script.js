function breaking() {
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
                      <th>Source</th>
                      <th>Date and Time</th>
                      <th>Tweet</th>
                      <th>Url</th>
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
                      <td>${tweet.username}</td>
                      <td>${tweet.date}</td>
                      <td>${tweet.content}</td>
                      <td><a href="${tweet.sourceUrl}" target="_blank">${tweet.sourceUrl}</a></td>
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

async function getNews() {
  const username = document.getElementById("username").value;

    const res = await fetch(`/latest?username=${encodeURIComponent(username)}`);
    // const res = await fetch(`/latest?limit=${limit}`);
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
                   <th>Url</th>
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
            <td>${t.sourceUrl}</td>
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

  document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("theme-toggle");
    const html = document.documentElement; // use <html> or <body>

    if(localStorage.getItem("theme") === "dark") {
        html.classList.add("dark");
    }

    toggleBtn.addEventListener("click", () => {
      html.classList.toggle("dark");

      // optional: change icon
      const icon = toggleBtn.querySelector("i");
      if (html.classList.contains("dark")) {
        localStorage.setItem("theme","dark");
        icon.classList.replace("fa-moon", "fa-sun");
        
      } else {
        localStorage.setItem("theme","light");
        icon.classList.replace("fa-sun", "fa-moon");
      }
    });
  });
  