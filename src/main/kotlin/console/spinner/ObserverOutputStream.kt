package console.spinner

import java.io.OutputStream

typealias Subscribers = (out: OutputStream) -> Unit

class ObserverOutputStream(
    private val innerStream: OutputStream
) : OutputStream() {
    private val beforeWriteSubscribers = mutableListOf<Subscribers>()
    private val afterWriteSubscribers = mutableListOf<Subscribers>()

    fun subscribeToBeforeWrite(subscriber: Subscribers) = beforeWriteSubscribers.add(subscriber)
    fun subscribeToAfterWrite(subscriber: Subscribers) = afterWriteSubscribers.add(subscriber)

    override fun write(byte: Int) {
        beforeWriteSubscribers.forEach{ subscriber -> subscriber(this) }
        innerStream.write(byte)
        afterWriteSubscribers.forEach{ subscriber -> subscriber(this) }
    }

    override fun write(b: ByteArray) {
        beforeWriteSubscribers.forEach{ subscriber -> subscriber(this) }
        innerStream.write(b, 0, b.size)
        afterWriteSubscribers.forEach{ subscriber -> subscriber(this) }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        beforeWriteSubscribers.forEach{ subscriber -> subscriber(this) }
        innerStream.write(b, off, len)
        afterWriteSubscribers.forEach{ subscriber -> subscriber(this) }
    }

    override fun flush() {
        innerStream.flush()
    }

    override fun close() {
        innerStream.close()
    }
}
