const functions = require('firebase-functions')
const admin = require('firebase-admin')

require('dotenv').config();

let serviceAccount = { 
    type: "service_account",
    project_id: "first-project-df1fb",
    private_key_id: "5a979d13fd6828b605648a55f6f81b744d9e3266",
    private_key: "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDCyj8T9PmJCYFX\nNnKNY5C9tdrtiOzsPXAEo1wyBokbHeRNtwW1syC8GjMnCd+MLdZjKym8jOvqAKT0\nkPpFZSLrlKL51HhyW+Nn9/VLtLDC7OAmBEmIPaoSqPdbXqaipwpG126mUVW7siZH\nmnY1ZEdDU+KmGBwoSjmOJ6FMFkF0ASQ8hEyntJ6qx/QME5USRF156Ct1FXUl6tbr\nICj4KTQ1Tp2ezI1ds3VkpYrrRfkHYMm6teQ0/a3/EVXF46bejuhA/Il86YAbJGZ7\n3zNZf2BZHBk0vhIeWVJ6V6Jl5NjzC1XxPhbRxWQYiMukbBqnhVoJexo4XoWTHiGe\nu36w0fmbAgMBAAECggEADI6r0byj6F6WM0oD/EzULc61it7RYaQ1fpVzbswqD/co\n2sCD3AQf7GoGxl6ZRgWsdPjGEez5xm+WzFqbFiWTdaFy5W0tLwSKdlwVuNtj3v6u\nfCgf/2on3SUXl5EHmz1U6PhsnmJohv2dZyKBII1sfkQmb9oZ2P6tk2L6cuSNmqdD\nts8CRtLue2z2I0UI+kj8cWbbeZUFQ2ERVgBVsotn+5MTrzwWX0vS1fIXm2qeM7Qe\nkyIMZp3rL4Kvq03xFMev210UTzsf0f80Qmc3Zd84bOYB8RGjadmCEKZsCDW7z56n\n5JLL4EzV1svfVCdPqOUJRqvnNEA9aHBV7xYr1xehyQKBgQDwnGuIaLnZ8Za9w/uo\nbky2r0OVz3kzS7yL4uWMa648/t+fsQWtjIkEyEZE6LHyaKUUj7QpKvsXiHe820Qj\nQZe0VKSgyZqYUpqERxHJxAZYgMYjlfKoAaqARBZ7n4cECe12KjBetifateJQMVmQ\nHNgdByRAiAyW4boXG3F0SgvlIwKBgQDPP5axMYuqG4CBpsvPec2E+B44fRYBGjnV\nzfCrdkREPvF7U1zrsb4PoJZ27rVNcKUQuiUBgtNa8NxJkgFFGQ5oY0RytnszswCY\nJI8P4F1jYcoSWVNkhSBBtkCTsGpr8qsOLx97aXPgYhFkKXeutZtxokIJv3pQbMdM\n5CvZ+9KNKQKBgQCoD5nMndmysVxpEAsnimNq9uBuM4ykl7IKw2Eyw8PgNuZb1Hny\nzsal4H20hBRHDXDA+98LIkpgFaIM99qQBbDA9J5I8Iha/NeQrVNvasxD5S6AjKwB\nTaiaDvTlRHg6CfUjJDTE8tbDnWeGO/IjBwKsw+A+csjYWrJwkWqdn1rPoQKBgFiC\nisPwKfqjNjWo24AnP8ua/6UWXtBnt+2Ev1Uq9CrFSvJlfti2FzqrVbdDaTDCe+xx\n4x4LFmLPXPUaouo0sj+IrZHkNye0OfvAGU0pgBjSia9ecMwMQuNth5TLkhe6Eq3D\nGEnnlnJsz4qcXx3BN7rHFjb9jKpOWquPAt9cueZJAoGAY9Y1PuLnOGs5V8m/dTJG\nYhnB1Fh3wCx/WMRetgbC7FOE+q8xhH6xOdMO5HwNv1mFDm3WnPry0Y1E8PZKgcY6\nOIQjQHsdObARGDjUXly9Xy+XOt40Z6gjj1fuWhjkP7bD1f+xiZ+tVqvZPkYkz2Zk\n0VUs2etyCAbiMG5rXSpn2Ss=\n-----END PRIVATE KEY-----\n",
    client_email: "firebase-adminsdk-pvgv5@first-project-df1fb.iam.gserviceaccount.com",
    client_id: "100835595878081586725",
    auth_uri: "https://accounts.google.com/o/oauth2/auth",
    token_uri: "https://oauth2.googleapis.com/token",
    auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
    client_x509_cert_url: "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-pvgv5%40first-project-df1fb.iam.gserviceaccount.com"
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://first-project-df1fb-default-rtdb.firebaseio.com/"
})

const request = require('request-promise')

    const naverRequestMeUrl = 'https://openapi.naver.com/v1/nid/me'
    
    function requestMe(naverAccessToken) {
      console.log('Requesting user profile from Naver API server.')
      return request({
        method: 'GET',
        headers: {
	'Authorization': 'Bearer ' + naverAccessToken,
	'X-Naver-Client-Id': 'DwBxwh3RiYjsumNlQaEo',
	'X-Naver-Client-Secret': 'vCN3fiVqPQ'
	},
        url: naverRequestMeUrl
      })
    }

  function updateOrCreateUser(userId, email, displayName, photoURL) {
    console.log('updating or creating a firebase user');
    const updateParams = {
      provider: 'NAVER',
      displayName: displayName,
    };
    if (displayName) {
      updateParams['displayName'] = displayName;
    } else {
      updateParams['displayName'] = email;
    }
    if (photoURL) {
      updateParams['photoURL'] = photoURL;
    }
    console.log(updateParams);
    return admin.auth().updateUser(userId, updateParams)
    .catch((error) => {
      if (error.code === 'auth/user-not-found') {
        updateParams['uid'] = userId;
        if (email) {
          updateParams['email'] = email;
        }
        return admin.auth().createUser(updateParams);
      }
      throw error;
    });
  }

  function createFirebaseToken(naverAccessToken) {
    return requestMe(naverAccessToken).then((response) => {
      const body = JSON.parse(response)
      console.log(body)
      const userId = `naver:${body.response.id}`
      if (!userId) {
        return res.status(404)
        .send({message: 'There was no user with the given access token.'})
      }
      let name = null
      let profileImage = null
      if (body.properties) {
        name = body.response.name
        profileImage = body.response.profile_image
      }
      return updateOrCreateUser(userId, body.response.email, name,
        profileImage)
    }).then((userRecord) => {
      const userId = userRecord.uid
      console.log(`creating a custom firebase token based on uid ${userId}`)
      return admin.auth().createCustomToken(userId, {provider: 'KAKAO'})
    })
  }

  exports.naverCustomAuth = functions.region('asia-northeast1').https
  .onRequest((req, res) => {
    const token = req.body.token
    if (!token) return resp.status(400).send({error: 'There is no token.'})
    .send({message: 'Access token is a required parameter.'})

    console.log(`Verifying naver token: ${token}`)
    createFirebaseToken(token).then((firebaseToken) => {
      console.log(`Returning firebase token to user: ${firebaseToken}`)
      res.send({firebase_token: firebaseToken});
    })

    return
  })