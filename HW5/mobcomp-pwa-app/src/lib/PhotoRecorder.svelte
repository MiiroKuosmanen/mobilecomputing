<script lang="ts">
    let imageUrl: string | undefined;
  
    async function handleFileChange(event: any) {
      const file = event.target.files[0];
      if (file) {
        const resizedImage = await resizeImage(file);
        imageUrl = URL.createObjectURL(resizedImage as Blob);
      }
    }
  
    async function resizeImage(file: any) {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = URL.createObjectURL(file);
        img.onload = () => {
          const canvas = document.createElement('canvas');
          const maxWidth = 50;
          const scaleSize = maxWidth / img.width;
          canvas.width = maxWidth;
          canvas.height = img.height * scaleSize;
  
          const ctx = canvas.getContext('2d');
          if (ctx) {
            ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
          }
          canvas.toBlob((blob) => {
            resolve(blob);
          }, 'image/jpeg', 0.85);
        };
        img.onerror = reject;
      });
    }
  </script>

  <label for="imageCapture">Upload Image</label>
  <input type="file" id="imageCapture" accept="image/*" capture="environment" on:change={handleFileChange}>
  {#if imageUrl}
    <!-- svelte-ignore a11y-img-redundant-alt -->
    <img src={imageUrl} alt="Selected photo" />
  {/if}
  