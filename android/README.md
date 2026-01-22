# Lynx Android Image Picker (Native Module)

This repository contains a **minimal native Android Image Picker module for Lynx**.

It exposes Android’s native image picker to Lynx JavaScript via a **custom native module**, allowing users to select one or multiple images and upload them (example uses Supabase).

This module was built because **Lynx currently does not provide a built-in Image Picker for Android**.

---

## ✨ Features

- 📷 Open native Android image picker
- 🖼️ Single or multiple image selection
- 🔁 Returns image URIs to JavaScript
- ☁️ Optional upload flow (example: Supabase)
- 🔌 Works in custom Android Lynx wrappers
- 🧩 Clean separation between JS and native code

---

## 📦 What this is (and what it is not)

### ✅ This **is**
- A **native module** (logic / API)
- Exposed via `@LynxMethod`
- Called from JavaScript
- No UI component involved

### ❌ This is **not**
- A native UI element
- A Lynx Host / Explorer plugin
- A cross-platform solution (Android only)

---


---

## 🧩 Using the Image Picker in TS / TSX

The native module is accessed through a small JavaScript wrapper.
This keeps your app safe when the native module is not available
(e.g. LynxGo or web).

### Example

```tsx
import { pickAvatarImage } from './nativeModules';

function ProfileAvatar({ accessToken, userId }: Props) {
  return (
    <view
      bindtap={() => {
        pickAvatarImage(
          accessToken,
          userId,
          (previewUri) => {
            console.log('Preview:', previewUri);
          },
          (uploadedUrl) => {
            console.log('Uploaded:', uploadedUrl);
          }
        );
      }}
    >
      <text>Change avatar</text>
    </view>
  );
}
```


📁 `example/ImagePickerExample.tsx`

```tsx
import '@lynx-js/react';
import { useState } from '@lynx-js/react';
import { pickSpotImages } from '../nativeModules';

export function ImagePickerExample({
  accessToken,
}: {
  accessToken: string;
}) {
  const [preview, setPreview] = useState<string | null>(null);
  const [uploaded, setUploaded] = useState<string | null>(null);

  return (
    <view style={{ padding: 16 }}>
      <view
        className="bg-accent rounded-xl p-4"
        bindtap={() => {
          pickSpotImages(
            accessToken,
            (json) => {
              setPreview(json);
            },
            (json) => {
              setUploaded(json);
            }
          );
        }}
      >
        <text className="text-white font-bold">
          Pick images
        </text>
      </view>

      {preview && (
        <text style={{ marginTop: 12 }}>
          Preview URIs: {preview}
        </text>
      )}

      {uploaded && (
        <text style={{ marginTop: 12 }}>
          Uploaded URLs: {uploaded}
        </text>
      )}
    </view>
  );
}
```

📁 `src/lib/nativeModules.ts`
```ts
type PreviewCallback = (uri: string | null) => void;
type UploadCallback = (url: string | null) => void;

type MultiPreviewCallback = (json: string | null) => void;
type MultiUploadCallback = (json: string | null) => void;

interface ImagePickerModuleNative {
  pickSpotImages?: (
    accessToken: string,
    onPreview: MultiPreviewCallback,
    onUploaded: MultiUploadCallback
  ) => void;

  pickAvatarImage?: (
    accessToken: string,
    userId: string,
    onPreview: PreviewCallback,
    onUploaded: UploadCallback
  ) => void;
}

interface NativeModulesGlobal {
  NativeModules?: {
    ImagePickerModule?: ImagePickerModuleNative;
  };
}

function getImagePicker(): ImagePickerModuleNative | null {
  const nm =
    (typeof NativeModules !== 'undefined'
      ? (NativeModules as NativeModulesGlobal['NativeModules'])
      : null) ||
    ((globalThis as NativeModulesGlobal)?.NativeModules ?? null);

  const picker = nm?.ImagePickerModule ?? null;

  if (!picker) {
    console.warn('[ImagePickerModule] not available');
    return null;
  }

  return picker;
}

export function pickSpotImages(
  accessToken: string,
  onPreview: MultiPreviewCallback,
  onUploaded: MultiUploadCallback
): boolean {
  const picker = getImagePicker();
  if (!picker?.pickSpotImages) return false;

  picker.pickSpotImages(accessToken, onPreview, onUploaded);
  return true;
}

export function pickAvatarImage(
  accessToken: string,
  userId: string,
  onPreview: PreviewCallback,
  onUploaded: UploadCallback
): boolean {
  const picker = getImagePicker();
  if (!picker?.pickAvatarImage) return false;

  picker.pickAvatarImage(accessToken, userId, onPreview, onUploaded);
  return true;
}
```