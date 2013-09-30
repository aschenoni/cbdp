#include <boost/thread/mutex.hpp>
#include <boost/thread/condition.hpp>


template <typename T> class ConcurrentQueue {
public:
	ConcurrentQueue(int size)
		: _prod(0), _consu(0), _full(0) {
			if (size <= 0)
				throw std::runtime_error("size should be greater than 0");
			_buf_size = size;
			_buf = new T[_buf_size];
	}

	~ConcurrentQueue() {
		delete[] _buf;
	}

	void put(T m) {
		boost::mutex::scoped_lock lk(_mutex);

		if (_full == _buf_size) {
			while (_full == _buf_size)
				_cond_not_full.wait(lk);
		}

		_buf[_prod] = m;
		_prod = (_prod+1) % _buf_size;
		++ _full;
		_cond_not_empty.notify_one();
	}

	T get() {
		boost::mutex::scoped_lock lk(_mutex);

		if (_full == 0) {
			while (_full == 0)
				_cond_not_empty.wait(lk);
		}

		T i = _buf[_consu];
		_consu = (_consu+1) % _buf_size;
		-- _full;
		_cond_not_full.notify_one();
		return i;
	}

private:
	boost::mutex _mutex;
	boost::condition _cond_not_full;
	boost::condition _cond_not_empty;
	int _prod, _consu, _full;
	T* _buf;
	int _buf_size;
};
