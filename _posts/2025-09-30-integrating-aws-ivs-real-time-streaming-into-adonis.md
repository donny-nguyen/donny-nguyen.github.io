# Integrating AWS IVS Real-Time Streaming into Adonis.js

Integrating AWS IVS Real-Time Streaming into an Adonis.js application involves combining frontend WebRTC capabilities with backend orchestration. Here's a step-by-step guide tailored to your stack:

---

### 🧩 1. Understand the Architecture
Amazon IVS Real-Time Streaming uses **Stages** and **Participants**. The **Web Broadcast SDK** handles joining, publishing, and subscribing to media streams in the browser. Adonis.js will serve as your backend for authentication, session management, and API orchestration.

---

### 🛠️ 2. Set Up AWS IVS
- Go to the [Amazon IVS Console](https://console.aws.amazon.com/ivs/home).
- Create a **Stage**.
- Generate **Participant Tokens** via the AWS SDK or CLI.
- Store your **Stage ARN**, **API keys**, and **region** securely in `.env`.

---

### ⚙️ 3. Backend: Adonis.js Integration
Use AWS SDK to generate participant tokens:

```ts
// adonis-app/app/Services/IvsService.ts
import { IVSRealTime } from '@aws-sdk/client-ivs-realtime';

const client = new IVSRealTime({
  region: 'us-east-1',
  credentials: {
    accessKeyId: Env.get('AWS_ACCESS_KEY_ID'),
    secretAccessKey: Env.get('AWS_SECRET_ACCESS_KEY'),
  },
});

export async function createParticipantToken(stageArn: string, userId: string) {
  const response = await client.createParticipantToken({
    stageArn,
    userId,
    capabilities: ['PUBLISH', 'SUBSCRIBE'],
  });
  return response.participantToken;
}
```

Expose this via a controller:

```ts
// adonis-app/app/Controllers/Http/IvsController.ts
import { createParticipantToken } from 'App/Services/IvsService';

export default class IvsController {
  public async token({ request }) {
    const { userId } = request.only(['userId']);
    const token = await createParticipantToken(Env.get('IVS_STAGE_ARN'), userId);
    return { token };
  }
}
```

---

### 🎥 4. Frontend: Use IVS Web Broadcast SDK
Install the SDK:

```bash
npm install amazon-ivs-web-broadcast
```

Use it in your frontend (React, Vue, or vanilla JS):

```ts
import {
  Stage,
  LocalParticipant,
  RemoteParticipant,
  StageEvents,
} from 'amazon-ivs-web-broadcast';

const stage = new Stage(token, {
  participantToken: token,
});

stage.on(StageEvents.ParticipantJoined, (participant) => {
  console.log('Participant joined:', participant);
});

stage.join();
```

Refer to [AWS IVS Web Broadcast SDK Guide](https://docs.aws.amazon.com/ivs/latest/RealTimeUserGuide/broadcast-web.html) for full API usage.

---

### 🧪 5. Test with AWS Sample App
Check out the [Amazon IVS Real-Time Web Demo](https://github.com/aws-samples/amazon-ivs-real-time-for-web-demo) for a working example. You can adapt its frontend logic to your own UI and use Adonis.js to serve tokens securely.

---

### 🧠 Bonus Tips
- Use Adonis.js middleware to validate users before issuing tokens.
- Consider using WebSockets or polling to monitor participant activity.
- For advanced use cases (e.g., server-side composition), explore IVS HLS streams and media processing with FFmpeg.