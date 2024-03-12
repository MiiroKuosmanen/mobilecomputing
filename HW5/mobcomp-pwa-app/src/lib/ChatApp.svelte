<script lang="ts">
    import { writable } from 'svelte/store';
  
    let userImage = writable(null);
    let username = 'User1';
  
    function handleFileChange(event: any) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target && e.target.result && typeof e.target.result === 'string') {
            userImage.set(e.target.result);
          }
        };
        reader.readAsDataURL(file);
      }
    }
  
    function showUsername() {
      alert(`Username: ${username}`);
    }
  </script>
  
  <input type="file" accept="image/*" on:change={handleFileChange}>
  {#if $userImage}
<button type="button" on:click={showUsername}>
    <!-- svelte-ignore a11y-img-redundant-alt -->
    <img src="{$userImage}" alt="User picture" style="width: 100px; height: auto;">
</button>
  {/if}
  