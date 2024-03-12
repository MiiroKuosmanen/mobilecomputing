<script>
    // Function to navigate back to the HomePage
    function navigateBack() {
      window.location.hash = '#';
    }
  </script>
  
  <h1>Welcome to 122</h1>
  <button on:click={navigateBack}>Return</button>
  