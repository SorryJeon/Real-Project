protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    inputName = (EditText) findViewById(R.id.name);
    
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReferenceFromUrl("gs://realtimedatabase-3d54a.appspot.com");

    //다운로드할 파일을 가르키는 참조 만들기
    StorageReference pathReference = storageReference.child("picture/1483868222492.JPEG");

    //Url을 다운받기
    pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
        @Override
        public void onSuccess(Uri uri) {
            Toast.makeText(getApplicationContext(), "다운로드 성공 : "+ uri, Toast.LENGTH_SHORT).show();
            inputName.setText(uri.toString());

        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getApplicationContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
        }
    });
    
    //휴대폰 로컬 영역에 저장하기
    try {
        final File localFile = File.createTempFile("images", "jpg" );
        pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), "파일 저장 성공", Toast.LENGTH_SHORT).show();
                inputName.setText(localFile.getPath());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "파일 저장 실패", Toast.LENGTH_SHORT).show();
            }
        });
    } catch (IOException e) { Toast.makeText(getApplicationContext(), "예외가 발생했다 씨!!!!", Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }


}


출처: https://charactermail.tistory.com/67 [문자메일의 블로그]