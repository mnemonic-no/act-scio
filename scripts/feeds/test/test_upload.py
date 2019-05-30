"""Tests for the upload module"""

import io

import upload


def test_read_as_base64():
    """Test that the base64 encoding function allways returns a proper string"""

    tests = [(b'\x00\x00', 'AAA='),
             (b'test', 'dGVzdA=='),
             (b'\x00\xFF\x0A', 'AP8K')]

    for byte_content, encoded_content in tests:
        file_like = io.BytesIO(byte_content)
        encoded = upload.read_as_base64(file_like)
        assert encoded == encoded_content


def test_to_scio_submit_post_data():
    """Test that the maps to send to scio is on the correct form"""

    tests = [(b'\x00\x00', 'test.txt', 'AAA='),
             (b'test', 'something.bin', 'dGVzdA=='),
             (b'\x00\xFF\x0A', 'thisisafile.pck', 'AP8K')]

    for byte_content, file_name, encoded_content in tests:
        file_like = io.BytesIO(byte_content)
        my_map = upload.to_scio_submit_post_data(file_like, file_name)

        assert my_map == {'content': encoded_content, 'filename': file_name}


def test_metadata():

    meta_tests = [
        {
            "link": "https://blog.fox-it.com/2018/12/18/your-trust-our-signature/",
            "source": "Fox-IT International blog",
            "creation-date": "2018-12-18T13:35:42",
            "title": "Your trust, our signature",
            "links": [
                "https://blog.fox-it.com/2018/12/18/your-trust-our-signature/",
                "https://en.wikipedia.org/wiki/Email_spoofing",
                "https://github.com/fox-it/signed-phishing-email",
                "https://github.com/elceef/dnstwist",
                "https://www.rijksoverheid.nl/onderwerpen/digitale-overheid/vraag-en-antwoord/wat-is-een-elektronische-handtekening",
                "https://m2crypto.readthedocs.io/en/latest/howto.smime.html#m2crypto-smime"
            ],
            "partial_feed": False
        },
        {
            "link": "https://blog.erratasec.com/2019/05/your-threat-model-is-wrong.html",
            "source": "Errata Security",
            "creation-date": "2019-05-30T01:16:00",
            "title": "Your threat model is wrong",
            "links": [
                "https://t.co/eRYPZ9qkzB",
                "https://t.co/Q1aqCmkrWL",
                "https://twitter.com/briankrebs/status/1133844866834808834?ref_src=twsrc%5Etfw",
                "https://twitter.com/tyler_pieron/status/1133877429817237506?ref_src=twsrc%5Etfw",
                "https://twitter.com/nicoleperlroth/status/1133578305653813248?ref_src=twsrc%5Etfw"
            ],
            "partial_feed": False
        }
    ]

    file_pairs = [
        ('test/data/Your_trust_our_signature.html', 'test/data/Your_trust_our_signature.meta'),
        ('test/data/Your_threat_model_is_wrong.html', 'test/data/Your_threat_model_is_wrong.meta')
    ]

    res = upload.metadata(file_pairs)

    for i, _ in enumerate(res):
        assert res[i][1] == meta_tests[i]


def test_get_files():

    candidates = upload.get_files(['test/data'])

    sha256s = ['3a69d80a131119ac39bd37a6a341c4e117382ed55d5ea18fd31faad96f8a3c14',
               '7d4bd18144c36fb717322cd57593952b029b2ae7b38f108464f4cf3974a03215']

    assert len(candidates) == 2
    for candidate in candidates:
        assert 'link' in candidate.metadata
        assert isinstance(candidate.metadata.get('link', None), str)
        assert 'source' in candidate.metadata
        assert isinstance(candidate.metadata.get('source', None), str)
        assert 'creation-date' in candidate.metadata
        assert isinstance(candidate.metadata.get('creation-date', None), str)
        assert 'title' in candidate.metadata
        assert isinstance(candidate.metadata.get('title', None), str)
        assert 'links' in candidate.metadata
        assert isinstance(candidate.metadata.get('links', None), list)
        assert 'partial_feed' in candidate.metadata
        assert isinstance(candidate.metadata.get('partial_feed', None), bool)

        assert candidate.sha256() in sha256s
